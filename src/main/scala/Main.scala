// External imports
import net.dv8tion.jda.api.{JDABuilder, JDA}
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag

// Internal imports
import discord.Bot
import config.BotConfig
import utils.Logger

// Scala imports
import scala.util.{Try, Success, Failure}

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
      .createDefault(
        config.discordToken,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS
      )
      .setMemberCachePolicy(MemberCachePolicy.ALL)
      .enableCache(CacheFlag.MEMBER_OVERRIDES)
      .addEventListeners(bot)
      .build()

    (bot, jda)
  }.map(startBot)
    .recover(exception =>
      Logger.errorWithException("Failed to instantiate bot", exception)
      sys.exit(1)
    )

def startBot(bot: Bot, jda: JDA): Unit =
  Try {
    jda.awaitReady()
    Logger.info("Bot is now running! Press Ctrl+C to stop.")

    sys.addShutdownHook { stopBot(bot, jda) }

    Thread.currentThread().join()
  }.recover {
    case _: InterruptedException =>
      Logger.info("Bot interrupted")
      stopBot(bot, jda)
    case exception =>
      Logger.errorWithException("Bot runtime error", exception)
      stopBot(bot, jda)
  }

def stopBot(bot: Bot, jda: JDA): Unit =
  Logger.info("Shutting down gracefully...")
  bot.shutdown()
  jda.shutdown()
