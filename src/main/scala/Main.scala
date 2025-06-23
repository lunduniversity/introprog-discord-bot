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
  BotConfig
    .load()
    .map(instantiateBot)
    .recover(exception =>
      Logger.errorWithException("Failed to load configuration", exception)
      sys.exit(1)
    )

def instantiateBot(config: BotConfig): Unit =
  Try {
    val bot = Bot(config)
    val jda = JDABuilder
      .createLight(
        config.discordToken,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS
      )
      .addEventListeners(bot)
      .build()

    (bot, jda)
  }.map(runBot)
    .recover(exception =>
      Logger.errorWithException("Failed to instantiate bot", exception)
      sys.exit(1)
    )

def runBot(bot: Bot, jda: JDA): Unit = ???
