package dbot

import java.util.concurrent.{Executors, ExecutorService}
import java.nio.file.{
  FileSystems,
  Path,
  Paths,
  StandardWatchEventKinds,
  WatchKey,
  WatchService
}
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.ListBuffer

object FileWatcher:
  private val dataDirectory = Paths.get("data").toAbsolutePath
  private val watchers = ListBuffer[FileWatcher]()

  private val executorService = Executors.newSingleThreadExecutor()
  private val ec = ExecutionContext.fromExecutor(executorService)
  private val watchService = FileSystems.getDefault.newWatchService()
  private val watchKey = dataDirectory.register(
    watchService,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_CREATE
  )

  // Start watching immediately when object is loaded
  Future {
    while (!Thread.currentThread().isInterrupted) do
      Try {
        val key = watchService.take()
        key.pollEvents.forEach { event =>
          val kind = event.kind
          if kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE
          then
            val filename = event.context.asInstanceOf[Path]
            Thread.sleep(100) // Short delay to ensure write completes
            watchers.foreach { watcher =>
              if filename.toString == watcher.fileName
              then
                watcher.loadData()
                watcher.logReloadMessage(filename.toString)
            }
        }
        key.reset()
      }.recover { case ex =>
        if !Thread.currentThread.isInterrupted then
          Logger.errorWithException("Error watching file: ", ex)
      }
  }(using ec)

  def register(watcher: FileWatcher): Unit =
    watchers += watcher

  def shutdown(): Unit =
    Try {
      watchKey.cancel()
      watchService.close()
      executorService.shutdown()
    }.recover { case ex =>
      Logger.errorWithException("Error during file watcher shutdown: ", ex)
    }
    watchers.clear()

trait FileWatcher:
  protected def fileName: String
  protected def loadData(): Unit
  protected def logReloadMessage(filename: String): Unit

  protected lazy val filePath: Path =
    Paths.get("data", fileName).toAbsolutePath

  FileWatcher.register(this)
