// External imports
import net.dv8tion.jda.api.{JDABuilder, JDA}
import net.dv8tion.jda.api.requests.GatewayIntent

// Scala imports
import scala.util.{Try, Success, Failure}

// Internal imports
import discord.Bot
import config.BotConfig
import utils.Logger

@main def run(): Unit =
  BotConfig.load() match
    case Success(config) => instantiateBot(config)
    case Failure(exception) =>
      Logger.errorWithException("Failed to load configuration", exception)
      sys.exit(1)

def instantiateBot(config: BotConfig): Unit =
  Try {
    val bot = Bot(config)
    val jda = JDABuilder
      .createLight(
        config.discordToken,
        GatewayIntent.GUILD_MESSAGES
      )
      .addEventListeners(bot)
      .build()

    (bot, jda)
  } match
    case Success((bot, jda)) => runBot(bot, jda)
    case Failure(exception) =>
      Logger.errorWithException("Failed to instantiate bot", exception)
      sys.exit(1)

def runBot(bot: Bot, jda: JDA): Unit = ???
