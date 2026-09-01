package org.saintqd.vineriumlib.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.saintqd.vineriumlib.VineriumLib;

import java.util.Optional;

public class MMAbilityData {

    private static io.lumine.mythic.api.mobs.GenericCaster virtualCaster = null;

    public static void registerVirtualCaster() {
        virtualCaster = new io.lumine.mythic.api.mobs.GenericCaster(new io.lumine.mythic.core.adapters.VirtualEntity(
                new io.lumine.mythic.api.adapters.AbstractLocation("world",0.0,0.0,0.0)));
    }

    public static io.lumine.mythic.api.skills.SkillMetadata prepareMMSkillData(Entity caster) {
        if (!VineriumLib.inst().isMythicMobsEnabled())
            return null;
        io.lumine.mythic.api.skills.SkillMetadata skillData;
        if (caster != null) {
            Optional<io.lumine.mythic.api.skills.SkillCaster> possibleMobCaster =
                    io.lumine.mythic.api.MythicProvider.get().getMobManager().getSkillCaster(caster.getUniqueId());
            io.lumine.mythic.api.skills.SkillCaster skillCaster = possibleMobCaster.orElseGet(() ->
                    new io.lumine.mythic.api.mobs.GenericCaster(io.lumine.mythic.bukkit.BukkitAdapter.adapt(caster)));
            skillData = new io.lumine.mythic.core.skills.SkillMetadataImpl(io.lumine.mythic.api.skills.SkillTrigger.get("CUSTOM"), skillCaster, skillCaster.getEntity());
            LivingEntity targetEntity = null;
            if (caster instanceof Player player)
                targetEntity = io.lumine.mythic.core.utils.MythicUtil.getTargetedEntity(player);
            // Если имеется объект в перекрестии - используем как цель, иначе используем как цель локацию игрока
            io.lumine.mythic.api.adapters.AbstractLocation abstractLocation = io.lumine.mythic.bukkit.BukkitAdapter.adapt(caster.getLocation());
            skillData.setOrigin(abstractLocation);
            if (targetEntity != null)
                skillData.setEntityTarget(io.lumine.mythic.bukkit.BukkitAdapter.adapt(targetEntity));
            else
                skillData.setLocationTarget(abstractLocation);
        }
        else {
            skillData = new io.lumine.mythic.core.skills.SkillMetadataImpl(io.lumine.mythic.api.skills.SkillTrigger.get("CUSTOM"), virtualCaster, null);
        }
        skillData.setPower(1F);
        return skillData;
    }

    public static boolean executeMMSkill(String skillName, io.lumine.mythic.api.skills.SkillMetadata data) {
        if (!VineriumLib.inst().isMythicMobsEnabled())
            return false;
        Optional<io.lumine.mythic.api.skills.Skill> possibleSkill = io.lumine.mythic.api.MythicProvider.get().getSkillManager().getSkill(skillName);
        if (possibleSkill.isPresent()) {
            io.lumine.mythic.api.skills.Skill skill = possibleSkill.get();
            if (skill.isUsable(data)) {
                skill.execute(data);
                return true;
            }
            else
                return false;
        }
        else {
            VineriumLib.inst().getLogger().warning("MythicMobs skill " + skillName + " does not exist.");
        }
        return false;
    }

    public static boolean checkIfMMSkillUsable(String skillName, io.lumine.mythic.api.skills.SkillMetadata data) {
        if (!VineriumLib.inst().isMythicMobsEnabled())
            return false;
        Optional<io.lumine.mythic.api.skills.Skill> possibleSkill = io.lumine.mythic.api.MythicProvider.get().getSkillManager().getSkill(skillName);
        if (possibleSkill.isPresent()) {
            io.lumine.mythic.api.skills.Skill skill = possibleSkill.get();
            return skill.isUsable(data);
        }
        else {
            VineriumLib.inst().getLogger().warning("MythicMobs skill " + skillName + " does not exist.");
        }
        return false;
    }

}
