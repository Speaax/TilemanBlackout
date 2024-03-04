package com.example;

import net.runelite.client.config.*;

import java.awt.*;


@ConfigGroup(ExampleConfig.GROUP)
public interface ExampleConfig extends Config {
	String GROUP = "exampleconfig";

	@ConfigSection(
			name = "Settings",
			description = "Change your settings",
			position = 1
	)
	String settingsSection = "Settings";
	@ConfigItem(
			keyName = "neighborTiles",
			name = "Neighbor Tiles",
			section = "Settings",
			description = "Show Neighbor tiles",
			position = 1
	)
	default boolean neighborTiles() {
		return false;
	}
	@ConfigItem(
			position = 2,
			keyName = "highlightTileColor",
			name = "Neighbor tile color",
			description = "The color to use for highlighting neighbor tiles",
			section = settingsSection
	)
	default Color highlightTileColor() {return new Color(255, 0, 255, 25);}
	@ConfigItem(
			position = 3,
			keyName = "alphaValue",
			name = "Transparency",
			description = "The alpha to use for highlighting neighbor tiles",
			section = settingsSection
	)
	@Range(min = 0, max = 255)
	default int alphaValue() {return 25;}

	@ConfigItem(
			position = 4,
			keyName = "drawDistance",
			name = "Draw Distance",
			description = "Neightbor Tiles draw distance",
			section = settingsSection
	)
	@Range(min = 0, max = 255)
	default int drawDistance() {return 25;}
	@ConfigSection(
			name = "Playstyle",
			description = "Change your playstyle settings",
			position = 2
	)
	String playstyleSection = "Playstyle";
	@ConfigItem(
			keyName = "XPPlayerTile",
			name = "Player tile XP",
			section = playstyleSection,
			description = "Change how much xp to unlock a tile, default = 5000",
			position = 111
	)
	default int XPForAPlayerTile() {
		return 1000;
	}

	@ConfigItem(
			keyName = "XPRandomTile",
			name = "Random tile XP",
			section = playstyleSection,
			description = "Change how much xp to unlock a random tile, default = 1000",
			position = 112
	)
	default int XPForARandomTile() {
		return 1000;
	}

	@ConfigSection(
			name = "MySQL",
			description = "Setup for MySQL database.",
			position = 3
	)
	String mySQLsettings = "MySQLconfig";
	@ConfigItem(
			keyName = "playerid",
			name = "playerID",
			section = mySQLsettings,
			description = "Unique number playerID for each player in the group.",
			position = 1
	)
	default int playerID() {
		return 1;
	}

	@ConfigItem(
			keyName = "enableMySQL",
			name = "Use a MySQL database?",
			section = mySQLsettings,
			description = "Enable to use a MySQL database",
			position = 2
	)
	default boolean enableMySQL()
	{
		return false;
	}
	@ConfigItem(
			keyName = "IP",
			name = "IP",
			section = mySQLsettings,
			description = "IP address for MYSQL database.",
			position = 3
	)
	default String ip() {
		return "";
	}
	@ConfigItem(
			keyName = "username",
			name = "Username",
			section = mySQLsettings,
			description = "Username to access your MySQL database",
			position = 4
	)
	default String username() {
		return "";
	}

	@ConfigItem(
			keyName = "password",
			name = "Password",
			section = mySQLsettings,
			description = "Password to access your MySQL database",
			position = 5
	)
	default String password() {
		return "";
	}
}