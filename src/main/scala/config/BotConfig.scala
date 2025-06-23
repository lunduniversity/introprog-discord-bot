package config

// Internal imports
import utils.Logger

// Scala imports
import scala.util.{Try, Success, Failure}

case class BotConfig(
    discordToken: String,
    discordGuildId: String,
    nicknameWarnChannelName: String
)

object BotConfig:
  def load(): Try[BotConfig] =
    for
      token <- getRequiredEnv("DISCORD_TOKEN")
      guildId <- getRequiredEnv("DISCORD_GUILD_ID")
      nicknameWarnChannelName <- getRequiredEnv("NICKNAME_WARN_CHANNEL_NAME")
    yield BotConfig(
      discordToken = token,
      discordGuildId = guildId,
      nicknameWarnChannelName = nicknameWarnChannelName
    )

  private def getRequiredEnv(key: String): Try[String] =
    sys.env.get(key).filter(_.nonEmpty) match
      case Some(value) => Success(value)
      case None =>
        Failure(
          IllegalStateException(
            s"Missing or empty $key environment variable! See README.md for setup."
          )
        )

  private def getOptionalEnv(key: String): Option[String] =
    sys.env.get(key).filter(_.nonEmpty)
