package dbot

import java.util.concurrent.CopyOnWriteArraySet
import scala.jdk.CollectionConverters.*
import java.nio.file.{Files, Path}
import scala.util.Try
import dbot.Constants

object Nicknames extends FileWatcher:
  private case class Name(first: String, last: String):
    def isValid(nickname: String): Boolean = s"$first $last" == nickname

  protected val fileName: String = "allowed-names.txt"

  private val allowedNames =
    CopyOnWriteArraySet[Name]() // Thread safe Set-isch Java collection

  protected def loadData(): Unit =
    Try {
      allowedNames.clear()
      if Files.exists(filePath) then 
        os.read
          .lines(os.Path(filePath))
          .map(_.trim)
          .filter(_.nonEmpty)
          .foreach(l =>
            val parts = l.split(' ')
            allowedNames.add(Name(parts(1), parts(0)))
          )
        Logger.info(
          s"Loaded ${allowedNames.size()} allowed names from ${filePath}"
        )
      else Logger.error(s"Names file not found: ${filePath}")
    }.recover { case ex =>
      Logger.errorWithException("Failed to load allowed names: ", ex)
    }

  protected def logReloadMessage(filename: String): Unit =
    Logger.info(s"Reloaded allowed names due to file change: $filename")

  def initialize(): Unit =
    Logger.info("Initializing Nicknames object...")
    loadData()

  def isValid(nickname: String): Boolean =
    allowedNames.asScala.exists(_.isValid(nickname))
