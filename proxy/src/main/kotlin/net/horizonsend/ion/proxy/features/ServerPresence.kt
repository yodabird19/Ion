package net.horizonsend.ion.proxy.features

import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.util.Favicon
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.URL
import javax.imageio.ImageIO

/**
 * @see com.comphenix.protocol.utility.MinecraftProtocolVersion
 **/
object ServerPresence : IonProxyComponent() {
	private const val primaryVersion = 765
	private const val primaryVersionName = "1.20.4"
	private val allowedVersions = intArrayOf(759, 760, 761, 762, 763, 764, 765, 765)

	private val messages =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }

	private val icon = Favicon.create(ImageIO.read(URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png")))

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onProxyPingEvent(event: ProxyPingEvent) = event.response.run {
		val clientVersion = event.connection.version
		version = ServerPing.Protocol(primaryVersionName, if (allowedVersions.contains(clientVersion)) clientVersion else primaryVersion)
		players = ServerPing.Players(
			PLUGIN.proxy.onlineCount + 1,
			PLUGIN.proxy.onlineCount,
			PLUGIN.proxy.players.map { ServerPing.PlayerInfo(it.name, it.uniqueId) }.toTypedArray()
		)
		descriptionComponent = TextComponent(
			*TextComponent.fromLegacyText("${PLUGIN.configuration.motdFirstLine}\n${messages.random()}")
		)
		setFavicon(icon)
	}
}
