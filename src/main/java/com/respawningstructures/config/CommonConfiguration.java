package com.respawningstructures.config;

import com.cupboard.config.ICommonConfig;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class CommonConfiguration implements ICommonConfig
{
    public Set<String>
      dungeonChestLoottables = new LinkedHashSet<>(Lists.newArrayList("chests/abandoned_mineshaft", "chests/bastion_bridge", "chests/bastion_hoglin_stable", "chests/bastion_other",
      "chests/bastion_treasure", "chests/ancient_city", "chests/ancient_city_ice_box", "chests/desert_pyramid", "chests/end_city_treasure", "chests/igloo_chest",
      "chests/jungle_temple_dispenser", "chests/jungle_temple", "chests/nether_bridge", "chests/pillager_outpost", "chests/simple_dungeon", "chests/stronghold_corridor",
      "chests/stronghold_crossing", "chests/stronghold_library", "chests/woodland_mansion"));

    public Set<String>
        respawnableStructureIDs = new LinkedHashSet<>(Lists.newArrayList());

    public boolean whitelist = false;
    public Set<String> blacklistedStructures = new LinkedHashSet<>();

    public Set<String> dimensionBlackList    = new LinkedHashSet<>();

    public int     minutesUntilRespawn           = 60 * 48;
    public double blockCountMod = 1.0;
    public boolean enableAutomaticRespawn        = true;
    public boolean increaseDifficultyWithRespawn = true;

    public CommonConfiguration()
    {
    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Enables automatic respawning of structures, default:true");
        entry.addProperty("enableAutomaticRespawn", enableAutomaticRespawn);
        root.add("enableAutomaticRespawn", entry);

        final JsonObject entry7 = new JsonObject();
        entry7.addProperty("desc:", "Sets the time after which a structure can respawn, the timer starts after the last activity within the structure. default:2880 minutes(48h)");
        entry7.addProperty("minutesUntilRespawn", minutesUntilRespawn);
        root.add("minutesUntilRespawn", entry7);

        final JsonObject entry11 = new JsonObject();
        entry11.addProperty("desc:", "Enables increased difficulty for mobs in respawned dungeons(mobs can get enchanted items/potions effects), default: true");
        entry11.addProperty("increaseDifficultyWithRespawn", increaseDifficultyWithRespawn);
        root.add("increaseDifficultyWithRespawn", entry11);

        final JsonObject entry8 = new JsonObject();
        entry8.addProperty("desc:",
            "Modifies the placed/broken block count, which can prevent respawn. Setting it to zero means respawn will ignore placed/broken blocks. Setting it higher than 1.0 makes it more likely that players breaking/placing things in the structure will prevent its respawn."
                + "By default it blocks respawn after 200+(scales slightly with structure size) blocks placed/broken. default: 1.0");
        entry8.addProperty("blockCountMod", blockCountMod);
        root.add("blockCountMod", entry8);

        final JsonObject entry10 = new JsonObject();
        entry10.addProperty("desc:",
            "List of blacklisted dimension ids, e.g. minecraft:overworld  Seperate multiple entries by , ");
        final JsonArray list10 = new JsonArray();
        for (final String name : dimensionBlackList)
        {
            list10.add(name);
        }
        entry10.add("dimensionBlackList", list10);
        root.add("dimensionBlackList", entry10);


        final JsonObject entry12 = new JsonObject();
        entry12.addProperty("desc:",
            "List of structure IDs that should respawn always respawn, no matter if a player build sth inside, has a portal there etc.: e.g. minecraft:mansion");
        final JsonArray list8 = new JsonArray();
        for (final String name : respawnableStructureIDs)
        {
            list8.add(name);
        }
        entry12.add("respawnableStructures", list8);
        root.add("respawnableStructures", entry12);

        final JsonObject entry6 = new JsonObject();
        entry6.addProperty("desc:",
          "List of blacklisted structures, those won't respawn ever: e.g. minecraft:mansion   Seperate multiple entries by , ");
        final JsonArray list6 = new JsonArray();
        for (final String name : blacklistedStructures)
        {
            list6.add(name);
        }
        entry6.add("blacklistedStructures", list6);
        root.add("blacklistedStructures", entry6);

        final JsonObject entry14 = new JsonObject();
        entry14.addProperty("desc:", "Changes the structure blacklist to a whitelist, default: false");
        entry14.addProperty("whitelist", whitelist);
        root.add("whitelist", entry14);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        minutesUntilRespawn = data.get("minutesUntilRespawn").getAsJsonObject().get("minutesUntilRespawn").getAsInt();
        enableAutomaticRespawn = data.get("enableAutomaticRespawn").getAsJsonObject().get("enableAutomaticRespawn").getAsBoolean();
        increaseDifficultyWithRespawn = data.get("increaseDifficultyWithRespawn").getAsJsonObject().get("increaseDifficultyWithRespawn").getAsBoolean();
        blockCountMod = data.get("blockCountMod").getAsJsonObject().get("blockCountMod").getAsDouble();

        blacklistedStructures = new HashSet<>();
        for (final JsonElement element : data.get("blacklistedStructures").getAsJsonObject().get("blacklistedStructures").getAsJsonArray())
        {
            blacklistedStructures.add(element.getAsString());
        }

        dimensionBlackList = new HashSet<>();
        for (final JsonElement element : data.get("dimensionBlackList").getAsJsonObject().get("dimensionBlackList").getAsJsonArray())
        {
            dimensionBlackList.add(element.getAsString());
        }

        respawnableStructureIDs = new HashSet<>();
        for (final JsonElement element : data.get("respawnableStructures").getAsJsonObject().get("respawnableStructures").getAsJsonArray())
        {
            respawnableStructureIDs.add(element.getAsString());
        }
    }
}
