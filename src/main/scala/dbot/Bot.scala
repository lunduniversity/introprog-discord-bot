package dbot

// External imports
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.Member

// Internal imports
import dbot.{BotConfig, Constants}
import dbot.{Logger, ScheduledTask}
import dbot.{Nicknames, Roles}

// Scala imports
import scala.util.{Try, Success, Failure}
import scala.jdk.CollectionConverters.*

// Java imports
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.entities.Guild

class Bot(botConfig: BotConfig) extends ListenerAdapter {

  private val scheduler: ScheduledExecutorService =
    Executors.newScheduledThreadPool(2)

  private class NicknameCheckTask(
      guild: Guild,
      memberId: String
  ) extends ScheduledTask {
    protected val taskName = s"NicknameCheck-$memberId"

    protected def executeTask(): Unit = {
      val refreshedMember = guild.retrieveMemberById(memberId).complete()
      checkMemberNickname(refreshedMember)
    }
  }

  override def onGuildMemberJoin(event: GuildMemberJoinEvent): Unit =
    val member = event.getMember
    val guild = event.getGuild
    val user = member.getUser

    if user.isBot then return

    Logger.info(
      s"Member ${member.getEffectiveName} joined guild ${guild.getName}"
    )

    val nicknameCheckTask = new NicknameCheckTask(guild, member.getId)
    scheduler.schedule(nicknameCheckTask, 5, TimeUnit.MINUTES)

    assignMemberRole(member)

  override def onGuildMemberUpdateNickname(
      event: GuildMemberUpdateNicknameEvent
  ): Unit =
    val newNickname = Option(event.getNewNickname)
    val member = event.getMember
    val user = member.getUser

    if user.isBot then return

    Logger.info(
      s"Nickname updated for ${member.getEffectiveName}: ${newNickname.getOrElse("None")}"
    )

    if !isNicknameValid(newNickname) then
      sendNicknameWarning(member, newNickname)

    val nonEveryoneRoles = member.getRoles.asScala.filterNot(_.isPublicRole)
    if nonEveryoneRoles.isEmpty then assignMemberRole(member)

  private def checkMemberNickname(member: Member): Unit =
    val nickname = Option(member.getNickname)
    val displayName = member.getEffectiveName

    Logger.info(s"Checking nickname for member: $displayName")

    if !isNicknameValid(nickname) then sendNicknameWarning(member, nickname)

  private def isNicknameValid(nickname: Option[String]): Boolean =
    nickname.map(Nicknames.isValid).getOrElse(false)

  private def sendNicknameWarning(
      member: Member,
      nickname: Option[String]
  ): Unit = {
    findWarningChannel(member.getGuild) match
      case Some(channel) =>
        val nicknameText = nickname.getOrElse(member.getEffectiveName)
        val message =
          s"${member.getAsMention}, ditt smeknamn `$nicknameText` följer inte formatet `Förnamn Efternamn`."

        sendTextChannelMessage(channel, message)
        Logger.info(
          s"Sent nickname warning to ${member.getEffectiveName} in channel ${channel.getName}"
        )

      case None =>
        Logger.error(
          s"Warning channel '${Constants.Nicknames.WARN_TEXT_CHANNEL_NAME}' not found in guild ${member.getGuild.getName}"
        )
  }

  private def findWarningChannel(
      guild: net.dv8tion.jda.api.entities.Guild
  ): Option[TextChannel] = {
    Try {
      guild
        .getTextChannelsByName(Constants.Nicknames.WARN_TEXT_CHANNEL_NAME, true)
        .asScala
        .headOption
    } match
      case Success(channel) => channel
      case Failure(ex) =>
        Logger.error(s"Error finding warning channel: ${ex.getMessage}")
        None
  }

  private def assignMemberRole(member: Member): Unit =
    val effectiveName = member.getEffectiveName
    Logger.info(s"Checking role assignment for member: $effectiveName")

    Roles.getRoleForUser(effectiveName) match {
      case Some(roleName) =>
        val guild = member.getGuild
        val roles = guild.getRolesByName(roleName, true).asScala

        if roles.nonEmpty then
          val role = roles.head
          guild
            .addRoleToMember(member, role)
            .queue(
              _ =>
                Logger.info(
                  s"Assigned role '$roleName' to ${member.getEffectiveName}"
                ),
              error =>
                Logger.errorWithException(
                  s"Failed to assign role '$roleName' to ${member.getEffectiveName}",
                  error
                )
            )
        else
          Logger.error(s"Role '$roleName' not found in guild ${guild.getName}")
      case None =>
        Logger.info(s"No role assignment found for member: $effectiveName")
    }

  private def sendTextChannelMessage(
      channel: TextChannel,
      message: String
  ): Unit = {
    Try {
      channel
        .sendMessage(message)
        .queue(
          _ =>
            Logger
              .info(s"Successfully sent message to channel ${channel.getName}"),
          error =>
            Logger.error(
              s"Failed to send message to channel ${channel.getName}: ${error.getMessage}"
            )
        )
    } match
      case Failure(ex) =>
        Logger.error(s"Exception while sending message: ${ex.getMessage}")
      case _ =>
  }

  def shutdown(): Unit =
    Try {
      scheduler.shutdown()
      if !scheduler.awaitTermination(10, TimeUnit.SECONDS) then
        scheduler.shutdownNow()
        Logger.warning(
          "Scheduler did not terminate gracefully, forced shutdown"
        )
      else Logger.info("Scheduler terminated gracefully")

    } match
      case Failure(ex) =>
        Logger.error(s"Error during shutdown: ${ex.getMessage}")
      case _ => Logger.info("Bot shutdown completed")

}
