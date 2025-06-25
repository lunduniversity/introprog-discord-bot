package utils

trait ScheduledTask extends Runnable:
  protected def taskName: String
  protected def executeTask(): Unit

  final def run(): Unit =
    try {
      Logger.debug(s"Starting scheduled task: $taskName")
      executeTask()
      Logger.debug(s"Completed scheduled task: $taskName")
    } catch {
      case ex: Exception =>
        Logger.error(
          s"Failed to execute scheduled task '$taskName': ${ex.getMessage}"
        )
    }
