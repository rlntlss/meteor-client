package meteor.plugins

import meteor.Configuration
import meteor.Main
import meteor.plugins.agility.AgilityPlugin
import meteor.plugins.apitest.ApiTestPlugin
import meteor.plugins.autobankpin.AutoBankPinPlugin
import meteor.plugins.autologin.AutoLoginPlugin
import meteor.plugins.autorun.AutoRunPlugin
import meteor.plugins.bank.BankPlugin
import meteor.plugins.boosts.BoostsPlugin
import meteor.plugins.combatlevel.CombatLevelPlugin
import meteor.plugins.devtools.DevToolsPlugin
import meteor.plugins.entityhider.EntityHiderPlugin
import meteor.plugins.fishing.FishingPlugin
import meteor.plugins.grounditems.GroundItemsPlugin
import meteor.plugins.guardiansoftherift.GuardiansOfTheRiftPlugin
import meteor.plugins.itemprices.ItemPricesPlugin
import meteor.plugins.keyboardbankpin.KeyboardBankPinPlugin
import meteor.plugins.minimap.MinimapPlugin
import meteor.plugins.mousetooltips.MouseTooltipPlugin
import meteor.plugins.neverlog.NeverLogoutPlugin
import meteor.plugins.npcaggrolines.NpcAggroLinesPlugin
import meteor.plugins.rsnhider.RsnHiderPlugin
import meteor.plugins.specbar.SpecBarPlugin
import meteor.plugins.statusbars.StatusBarsPlugin
import meteor.plugins.stretchedmode.StretchedModePlugin
import meteor.plugins.worldmap.WorldMapPlugin
import meteor.plugins.xptracker.XpTrackerPlugin
import net.runelite.client.plugins.herbiboars.HerbiboarPlugin
import net.runelite.client.plugins.slayer.SlayerPlugin
import rs117.hd.HdPlugin
import java.io.File
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarInputStream
import java.util.jar.Manifest


object PluginManager {
    var plugins = ArrayList<Plugin>()
    var loadedExternals = ArrayList<String>()
    init {
        init<ApiTestPlugin>()
        init<AgilityPlugin>()
        init<AutoBankPinPlugin>()
        init<AutoLoginPlugin>()
        init<AutoRunPlugin>()
        init<BankPlugin>()
        init<BoostsPlugin>()
        //init<ChatFilterPlugin>()
        init<CombatLevelPlugin>()
        init<DevToolsPlugin>()
        init<ExamplePlugin>()
        init<EntityHiderPlugin>()
        init<FishingPlugin>()
        init<GroundItemsPlugin>()
        init<HerbiboarPlugin>()
        init<ItemPricesPlugin>()
        init<KeyboardBankPinPlugin>()
        init<MinimapPlugin>()
        init<MouseTooltipPlugin>()
        init<NpcAggroLinesPlugin>()
        init<NeverLogoutPlugin>()
        init<RsnHiderPlugin>()
        init<SlayerPlugin>()
        init<SpecBarPlugin>()
        init<StatusBarsPlugin>()
        init<StretchedModePlugin>()
        init<GuardiansOfTheRiftPlugin>()
        init<WorldMapPlugin>()
        init<XpTrackerPlugin>()
        init<HdPlugin>()
    }

    private fun loadExternal(file: File) {
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()))
        val pluginName = file.name.split(".jar")[0]
        if (!loadedExternals.contains(pluginName)) {
            loadedExternals.add(pluginName)
            Main.logger.debug("Added ${file.name} to classpath")
            val jarStream = JarInputStream(file.inputStream())
            val mf: Manifest = jarStream.manifest
            val testPlugin = classLoader.loadClass(mf.mainAttributes.getValue("Main-Class")).newInstance()
            val plugin = testPlugin as Plugin
            if (plugins.any { p -> p.getName().equals(plugin.getName()) })
                throw RuntimeException("Duplicate plugin ${plugin::class.simpleName} not allowed")

            plugins.add(plugin)
            plugin.subscribeEvents()
            if (plugin.isEnabled())
                start(plugin)
        }
    }

    fun loadExternalPlugins() {
        val externalsDir = File(Configuration.METEOR_DIR, "externalplugins")
        val plugins = externalsDir.listFiles()
        plugins?.let {
            for (file in it) {
                file?.let { f ->
                    loadExternal(f)
                }
            }
        }
    }

    inline fun <reified T : Plugin> init() {
        val plugin = T::class.java.newInstance()
        if (plugins.filterIsInstance<T>().isNotEmpty())
                throw RuntimeException("Duplicate plugin ${plugin::class.simpleName} not allowed")

            plugins.add(plugin)
            plugin.subscribeEvents()
            if (plugin.isEnabled())
                start(plugin)
    }

    inline fun <reified T : Plugin> get(): T {
        return plugins.filterIsInstance<T>().first()
    }

    inline fun <reified T : Plugin> restart() {
        val it = plugins.filterIsInstance<T>().first()
        stop(it)
        start(it)
    }

    fun stop(plugin: Plugin) {
        plugin.stop()
        plugin.onStop()
    }

     fun start(plugin: Plugin) {
         plugin.onStart()
         plugin.start()
    }

    fun shutdown() {
        for (plugin in plugins) {
            stop(plugin)
        }
    }
}
