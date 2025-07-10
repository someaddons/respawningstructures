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
      respawnableStructureIDs = new LinkedHashSet<>(Lists.newArrayList());

    public boolean whitelist = false;
    public Set<String> blacklistedStructures = new LinkedHashSet<>();

    public Set<String> dimensionBlackList    = new LinkedHashSet<>();

    public int     minutesUntilRespawn = 60 * 96;
    public double blockCountMod = 1.0;
    public boolean enableAutomaticRespawn        = true;
    public boolean increaseDifficultyWithRespawn = true;
    public int     playerRespawnDist   = 200;
    public int     playerNearbyTime    = 15;
    public boolean logRespawns         = true;
    public boolean needReload = true;

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
        entry7.addProperty("desc:", "Sets the time after which a structure can respawn, the timer starts after the last activity within the structure. default:5760 minutes(96h)");
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

        final JsonObject entry17 = new JsonObject();
        entry17.addProperty("desc:",
            "Sets the minimum distance from player respawn position(bed e.g.) to allow structure respawning, 0 to disable this. default: 200 blocks");
        entry17.addProperty("playerRespawnDist", playerRespawnDist);
        root.add("playerRespawnDist", entry17);

        final JsonObject entry18 = new JsonObject();
        entry18.addProperty("desc:",
            "Prevents respawns in too busy player areas, tracks how much percent of the respawntime a player was nearby loading the chunk. Resets respawn time counter when it prevent a structure respawn. default: 15 percent, range 0-100");
        entry18.addProperty("playerNearbyTime", playerNearbyTime);
        root.add("playerNearbyTime", entry18);

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
            "List of structure IDs(or tags of structures) that should respawn always respawn, no matter if a player build sth inside, has a portal there etc.: e.g. minecraft:mansion");
        final JsonArray list8 = new JsonArray();
        for (final String name : respawnableStructureIDs)
        {
            list8.add(name);
        }
        entry12.add("respawnableStructures", list8);
        root.add("respawnableStructures", entry12);

        final JsonObject entry6 = new JsonObject();
        entry6.addProperty("desc:",
            "List of blacklisted structures(or tags of structures), those won't respawn ever: e.g. minecraft:mansion   Seperate multiple entries by , ");
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

        final JsonObject entry19 = new JsonObject();
        entry19.addProperty("desc:", "Log structure respawns happening to the latest.log, default: true");
        entry19.addProperty("logRespawns", logRespawns);
        root.add("logRespawns", entry19);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        needReload = true;
        minutesUntilRespawn = data.get("minutesUntilRespawn").getAsJsonObject().get("minutesUntilRespawn").getAsInt();
        playerRespawnDist = data.get("playerRespawnDist").getAsJsonObject().get("playerRespawnDist").getAsInt();
        playerNearbyTime = data.get("playerNearbyTime").getAsJsonObject().get("playerNearbyTime").getAsInt();
        enableAutomaticRespawn = data.get("enableAutomaticRespawn").getAsJsonObject().get("enableAutomaticRespawn").getAsBoolean();
        logRespawns = data.get("logRespawns").getAsJsonObject().get("logRespawns").getAsBoolean();
        whitelist = data.get("whitelist").getAsJsonObject().get("whitelist").getAsBoolean();
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
