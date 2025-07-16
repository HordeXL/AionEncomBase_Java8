package com.aionemu.gameserver.network.aion.gmhandler;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.xml.JAXBUtil; 
import com.aionemu.gameserver.dataholders.DataManager; 
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import java.io.File;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Comando GM para adicionar uma skill combinada a um jogador.
 *
 * @author ginho1 (lógica original)
 * @author Dezalmado (adaptação para CmdGMHandler e correções de compilação)
 */
public final class CmdCombineSkill extends AbstractGMHandler {


	private static final File SKILLS_XML_FILE = new File("./data/scripts/system/handlers/consolecommands/data/skills.xml");

	public CmdCombineSkill(Player admin, String params) {
		super(admin, params);
		run(); 
	}

	public void run() {

		String[] splitParams = params.split(" ");

		if (splitParams.length < 1) { 
			PacketSendUtility.sendMessage(admin, "Syntax: //combineskill <skill_name> [skill_level]");
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

		String skillName = splitParams[0];
		int skillLvl = 1; 

		if (splitParams.length > 1) { 
			try {
				skillLvl = Integer.parseInt(splitParams[1]);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "Skill level must be an integer.");
				return;
			}
		}

		SkillsXmlData data = null; 
		try {
		    data = JAXBUtil.deserialize(SKILLS_XML_FILE, SkillsXmlData.class); 
		} catch (Exception e) {
		    PacketSendUtility.sendMessage(admin, "Error loading skills data: " + e.getMessage());
		    e.printStackTrace(); 
		    return;
		}

		if (data == null) {
		    PacketSendUtility.sendMessage(admin, "Could not load skills data from XML.");
		    return;
		}

		XmlSkillTemplate xmlSkillTemplate = data.getSkillTemplate(skillName); 
        int skillId = 0;
		if (xmlSkillTemplate != null) {
			skillId = xmlSkillTemplate.getTemplateId();
		} else {
            PacketSendUtility.sendMessage(admin, "Skill '" + skillName + "' not found in XML data.");
            return;
        }

		if (skillId > 0) {
            SkillTemplate actualGameSkillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
            if(actualGameSkillTemplate != null) {
                player.getSkillList().addSkill(player, skillId, skillLvl); 
                PacketSendUtility.sendMessage(admin, "Skill '" + skillName + "' (ID: " + skillId + ", Level: " + skillLvl + ") successfully added to " + player.getName() + "!");
            } else {
                PacketSendUtility.sendMessage(admin, "Error: Skill ID " + skillId + " exists in XML but not in game's DataManager. Cannot add skill.");
            }
		} else {
			PacketSendUtility.sendMessage(admin, "Error: Skill ID could not be determined for '" + skillName + "'.");
		}
	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "", name = "XmlSkillTemplate") 
	private static class XmlSkillTemplate { 

		@XmlAttribute(name = "id", required = true)
		@XmlID
		private String id;

		@XmlAttribute(name = "name")
		private String name;

		private int skillId; 

		public String getName() {
			return name;
		}

		public int getTemplateId() { 
			return skillId;
		}

		public void setSkillId(int skillId) {
			this.skillId = skillId;
		}

		@SuppressWarnings("unused")
		void afterUnmarshal(Unmarshaller u, Object parent) {
			setSkillId(Integer.parseInt(id));
		}
	}

	@XmlRootElement(name = "skills")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SkillsXmlData { 

		@XmlElement(name = "skill")
		private List<XmlSkillTemplate> its; 

		public XmlSkillTemplate getSkillTemplate(String skill) { 
			for (XmlSkillTemplate it : getData()) { 
				if (it.getName().toLowerCase().equals(skill.toLowerCase()))
					return it;
			}
			return null;
		}

		protected List<XmlSkillTemplate> getData() { 
			return its;
		}
	}
}