package utils

import java.util.concurrent.{CopyOnWriteArraySet, Executors}
import java.nio.file.{
  FileSystems,
  Files,
  Path,
  Paths,
  StandardWatchEventKinds,
  WatchEvent,
  WatchKey,
  WatchService
}
import scala.util.{Try, Using}
import scala.concurrent.{ExecutionContext, Future}
import config.Constants

object Nicknames:
  private val allowedNames =
    CopyOnWriteArraySet[String]() // Thread safe Set-isch Java collection
  private val executorService = Executors.newSingleThreadExecutor()
  private implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutor(executorService)

  private val namesFilePath: Path =
    Paths.get(Constants.Nicknames.ALLOWED_NAMES_FILE_NAME).toAbsolutePath

  private val watchService: WatchService =
    FileSystems.getDefault.newWatchService()
  private val watchKey: WatchKey = namesFilePath.getParent.register(
    watchService,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_CREATE // This is needed because some text editors modify files by simply deleting then creating the file :O
  )

  private def loadNames(): Unit =
    Try {
      allowedNames.clear()
      if (Files.exists(namesFilePath)) {
        os.read
          .lines(os.Path(namesFilePath))
          .map(_.trim)
          .filter(_.nonEmpty)
          .foreach(allowedNames.add)
        Logger.info(
          s"Loaded ${allowedNames.size()} allowed names from ${namesFilePath}"
        )
      } else {
        Logger.error(s"Names file not found: ${namesFilePath}")
      }
    }.recover { case ex =>
      Logger.errorWithException("Failed to load allowed names: ", ex)
    }

  private def startWatching(): Future[Unit] = Future {
    while (!Thread.currentThread().isInterrupted) do
      Try {
        val key =
          watchService
            .take() // This is a blocking call, which means the loop won't be progressing unless an event occurs
        key.pollEvents.forEach { event =>
          val kind = event.kind
          if kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE
          then
            val filename = event.context.asInstanceOf[Path]
            if (filename.toString == namesFilePath.getFileName.toString) {
              Thread.sleep(100) // Short delay, ensures write completes
              loadNames()
              Logger.info(
                s"Reloaded allowed names due to file change: $filename"
              )
            }
        }
        key.reset()
      }.recover { case ex =>
        if !Thread.currentThread.isInterrupted then
          Logger.errorWithException("Error watching file: ", ex)
      }
  }

  loadNames()
  startWatching()

  def isValid(nickname: String): Boolean = allowedNames.contains(nickname)

  def shutdown(): Unit =
    Try {
      watchKey.cancel()
      watchService.close()
      executorService.shutdown()
      if !executorService.awaitTermination(
          5,
          java.util.concurrent.TimeUnit.SECONDS
        )
      then executorService.shutdownNow()
    }.recover { case ex =>
      Logger.errorWithException("Error during shutdown: ", ex)
    }
