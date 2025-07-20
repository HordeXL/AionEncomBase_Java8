package com.aionemu.gameserver.model;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.model.gameobjects.player.Player;

@XmlEnum
public enum PlayerClass {
	WARRIOR(0, true),
    GLADIATOR(1, false),
    TEMPLAR(2, false),
    SCOUT(3, true),
    ASSASSIN(4, false),
    RANGER(5, false),
    MAGE(6, true),
    SORCERER(7, false),
    SPIRIT_MASTER(8, false),
    PRIEST(9, true),
    CLERIC(10, false),
    CHANTER(11, false),

	// News Class 4.3/4.5
	TECHNIST(12, true),
    AETHERTECH(13, false),
    GUNSLINGER(14, false),
    MUSE(15, true),
    SONGWEAVER(16, false),
    ALL(17);

	private byte classId;
	private int idMask;
	private boolean startingClass;
    private String rusname;

	private PlayerClass(int classId) {
		this(classId, false);
	}

    PlayerClass(int classId, String rusname) {
        this(classId, false, rusname);
    }

	private PlayerClass(int classId, boolean startingClass) {
		this.classId = (byte) classId;
		this.startingClass = startingClass;
		this.idMask = (int) Math.pow(2, classId);
	}

    PlayerClass(int classId, boolean startingClass, String rusname) {
        this.classId = (byte) classId;
        this.startingClass = startingClass;
        this.idMask = (int) Math.pow(2, classId);
        this.rusname = rusname;
    }

	public byte getClassId() {
		return classId;
	}

	public static PlayerClass getPlayerClassById(byte classId) {
		for (PlayerClass pc : values()) {
			if (pc.getClassId() == classId) {
				return pc;
			}
		}
		throw new IllegalArgumentException("There is no player class with id " + classId);
	}

	public boolean isStartingClass() {
		return startingClass;
	}

	public static PlayerClass getStartingClassFor(PlayerClass pc) {
		switch (pc) {
		case ASSASSIN:
		case RANGER:
			return SCOUT;
		case GLADIATOR:
		case TEMPLAR:
			return WARRIOR;
		case CHANTER:
		case CLERIC:
			return PRIEST;
		case SORCERER:
		case SPIRIT_MASTER:
			return MAGE;
		// News Class 4.3/4.5
		case SONGWEAVER:
			return MUSE;
		case AETHERTECH:
		case GUNSLINGER:
			return TECHNIST;
		case SCOUT:
		case WARRIOR:
		case PRIEST:
		case MAGE:
		case MUSE:
		case TECHNIST:
			return pc;
		default:
			throw new IllegalArgumentException("Given player class is starting class: " + pc);
		}
	}

	public static PlayerClass getPlayerClassByString(String fieldName) {
		for (PlayerClass pc : values()) {
			if (pc.toString().equals(fieldName)) {
				return pc;
			}
		}
		return null;
	}

	public int getMask() {
		return idMask;
	}

    /**
    * Reintroduced for compatibility.
    * Returns the class type (PHYSICAL or MAGIC) based on the player's class.
    * May require more detailed logic depending on the original usage.
    */
    public String getClassType(Player player) {
        String type = null;
        switch (player.getPlayerClass()) {
            case TEMPLAR:
            case ASSASSIN:
            case RANGER:
            case GLADIATOR:
            case GUNSLINGER:
                type = "PHYSICAL";
                break;
            case SORCERER:
            case CHANTER:
            case CLERIC:
            case SPIRIT_MASTER:
            case SONGWEAVER:
            case AETHERTECH:
                type = "MAGICAL";
                break;
            default:
             /**
             If the class is not recognized, you may want to return a default value,
             throw an exception, or return null, depending on the desired behavior.
             For now, return null.
             */
                break;
        }
        return type;
    }

    public String getEnumName() {
        return rusname;
    }
}