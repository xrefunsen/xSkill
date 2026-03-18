package dev.xskill.xskill;

import dev.xskill.xskill.commands.XSkillCommand;
import dev.xskill.xskill.data.PlayerDataStore;
import dev.xskill.xskill.services.CooldownService;
import dev.xskill.xskill.services.FrozenService;
import dev.xskill.xskill.services.LevelService;
import dev.xskill.xskill.services.SwordRegistry;
import dev.xskill.xskill.util.Keys;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class XSkillPlugin extends JavaPlugin {
    private SwordRegistry swordRegistry;
    private CooldownService cooldownService;
    private FrozenService frozenService;
    private PlayerDataStore playerDataStore;
    private LevelService levelService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Keys.init(this);
        this.playerDataStore = new PlayerDataStore(this);
        this.levelService = new LevelService(this, playerDataStore);
        this.cooldownService = new CooldownService();
        this.frozenService = new FrozenService();
        this.swordRegistry = new SwordRegistry(this);
        swordRegistry.reload();

        getServer().getPluginManager().registerEvents(new XSkillListener(this), this);
        PluginCommand cmd = getCommand("xskill");
        if (cmd != null) {
            XSkillCommand handler = new XSkillCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    @Override
    public void onDisable() {
        playerDataStore.flushAll();
        frozenService.clear();
    }

    public SwordRegistry swords() {
        return swordRegistry;
    }

    public CooldownService cooldowns() {
        return cooldownService;
    }

    public FrozenService frozen() {
        return frozenService;
    }

    public PlayerDataStore data() {
        return playerDataStore;
    }

    public LevelService levels() {
        return levelService;
    }
}
