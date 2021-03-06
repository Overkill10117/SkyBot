/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.essentials

import com.sun.management.OperatingSystemMXBean
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import oshi.SystemInfo
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import kotlin.math.floor

class StatsCommand : Command() {
    private val oshi = SystemInfo().operatingSystem

    init {
        this.category = CommandCategory.UTILS
        this.name = "stats"
        this.help = "Shows some nerdy statistics about the bot"
    }

    override fun execute(ctx: CommandContext) {
        val shardManager = ctx.shardManager
        val connectedVC = shardManager.shardCache.map { shard ->
            shard.voiceChannelCache.filter { vc ->
                vc.members.contains(vc.guild.selfMember)
            }.count()
        }.sum()

        val uptimeLong = ManagementFactory.getRuntimeMXBean().uptime
        val serverUptimeString = AirUtils.getUptime(oshi.systemUptime * 1000)

        val platformMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
        val cores = platformMXBean.availableProcessors
        val serverCpuUsage = DecimalFormat("###.###%").format(platformMXBean.cpuLoad)
        val serverMem = (platformMXBean.totalMemorySize shr 20).toDouble()
        val serverMemUsage = serverMem - (platformMXBean.freeMemorySize shr 20)
        val serverMemPercent = floor((serverMemUsage / serverMem) * 100.0)

        val memoryBean = ManagementFactory.getMemoryMXBean()
        val jvmCpuUsage = DecimalFormat("###.###%").format(platformMXBean.processCpuLoad)
        val jvmMemUsage = (memoryBean.heapMemoryUsage.used shr 20).toDouble()
        val jvmMemTotal = (memoryBean.heapMemoryUsage.max shr 20).toDouble()
        val jvmMemPercent = floor((jvmMemUsage / jvmMemTotal) * 100)
        val os = "${platformMXBean.name} ${platformMXBean.arch} ${platformMXBean.version}"

        val threadBean = ManagementFactory.getThreadMXBean()

        val embed = EmbedUtils.getDefaultEmbed()
            .addField(
                "Discord/bot Stats",
                """**Guilds cache:** ${shardManager.guildCache.size()}
                    |**User cache:** ${shardManager.userCache.size()}
                    |**TextChannel cache:** ${shardManager.textChannelCache.size()}
                    |**VoiceChannel cache:** ${shardManager.voiceChannelCache.size()}
                    |**GuildSetting cache:** ${ctx.variables.guildSettingsCache.size}
                    |**Playing music count:** $connectedVC
                    |**Uptime:** ${AirUtils.getUptime(uptimeLong)}
                """.trimMargin(),
                false
            )

            .addField(
                "Server stats",
                """**CPU cores:** $cores
                    |**CPU usage:** $serverCpuUsage
                    |**Ram:** ${serverMemUsage}MB / ${serverMem}MB ($serverMemPercent%)
                    |**System uptime:** $serverUptimeString
                    |**Operating System:** $os
                """.trimMargin(),
                false
            )

            .addField(
                "JVM stats",
                """**CPU usage:** $jvmCpuUsage
                    |**Threads:** ${threadBean.threadCount} / ${threadBean.peakThreadCount}
                            |**Ram:** ${jvmMemUsage}MB / ${jvmMemTotal}MB ($jvmMemPercent%)
                        """.trimMargin(),
                false
            )

        sendEmbed(ctx, embed)
    }
}
