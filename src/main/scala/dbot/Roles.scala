package dbot

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*
import java.nio.file.{Files, Path}
import scala.util.Try
import dbot.Constants
import dbot.Logger

object Roles extends FileWatcher:
  private enum RoleType:
    case Student, Handledare

  protected val fileName: String = "user-roles.txt"

  private val userRoles =
    ConcurrentHashMap[String, RoleType]()

  protected def loadData(): Unit =
    Try {
      userRoles.clear()
      if Files.exists(filePath) then
        val lines =
          os.read.lines(os.Path(filePath)).map(_.trim).filter(_.nonEmpty)
        var currentRole: Option[RoleType] = None

        lines.foreach { line =>
          line match
            case "Students"   => currentRole = Some(RoleType.Student)
            case "Handledare" => currentRole = Some(RoleType.Handledare)
            case userName if currentRole.isDefined =>
              userRoles.put(userName, currentRole.get)
            case _ =>
        }

        Logger.info(
          s"Loaded ${userRoles.size()} user role assignments from ${filePath}"
        )
      else Logger.error(s"Roles file not found: ${filePath}")
    }.recover { case ex =>
      Logger.errorWithException("Failed to load user roles: ", ex)
    }

  protected def logReloadMessage(filename: String): Unit =
    Logger.info(s"Reloaded user roles due to file change: $filename")

  def initialize(): Unit =
    Logger.info("Initializing Roles object...")
    loadData()

  def getRoleForUser(userName: String): Option[String] =
    Option(userRoles.get(userName)).map {
      case RoleType.Student    => Constants.Roles.STUDENT_ROLE_NAME
      case RoleType.Handledare => Constants.Roles.HANDLEDARE_ROLE_NAME
    }
