package discord

import config.BotConfig
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent

class Bot(config: BotConfig) extends ListenerAdapter {

  // Needed for setting "timer" for nicknames check
  override def onGuildMemberJoin(event: GuildMemberJoinEvent): Unit = ???

  // Needed for checking nicknames against nicknaming rules
  override def onGuildMemberUpdateNickname(
      event: GuildMemberUpdateNicknameEvent
  ): Unit = ???
}
