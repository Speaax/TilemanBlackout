package com.example;

import com.example.SQLite.Database;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(
		name = "TileMan (blackout)"
)

public class ExamplePlugin extends Plugin
{
	private WorldPoint lastPlayerLocation = null;
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxOverlay infoBoxOverlay;
	@Inject
	private UnlockableTilesOverlay unlockableTilesOverlay;


	@Inject
	private ClientToolbar clientToolbar;

	private Database database;


	public static List<WorldPoint> unlockedTiles = new ArrayList<>();
	public static List<WorldPoint> unlockableTiles = new ArrayList<>();

	public boolean started = false;
	@Override
	protected void startUp() throws Exception
	{
		this.database = new Database(this.config);
		database.initialize();
		//database.setupInitialCards();
		database.setupInitialUserStats();
		if (database.getStarted(1) == 1) {
			started = true;
		}
		loadTilesFromDatabase();

		overlayManager.add(infoBoxOverlay);
		overlayManager.add(unlockableTilesOverlay);

		ExamplePanel panel = new ExamplePanel(this);
		NavigationButton navButton = NavigationButton.builder()
				.tooltip("Tileman Panel")
				.icon(ImageUtil.loadImageResource(getClass(), "/icon.png"))
				.priority(70)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
		updateConfig();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(infoBoxOverlay);
		overlayManager.remove(unlockableTilesOverlay);
		reloadScene();
	}


	public void unlockDirection(String direction) {
		WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
		if (Objects.equals(direction, "N")) {
			checkAndUnlockTile(currentLocation.dy(1));
		} else if (Objects.equals(direction, "W")) {
			checkAndUnlockTile(currentLocation.dx(-1));
		}else if (Objects.equals(direction, "E")) {
			checkAndUnlockTile(currentLocation.dx(1));
		}else if (Objects.equals(direction, "S")) {
			checkAndUnlockTile(currentLocation.dy(-1));
		}
	}

	public void clearTiles() {
		//Never clearTilesByStatus(0), 0 means permanently unlocked tiles and should remain unlocked
		database.clearTilesByStatus(1); // Clear unlockable tiles
		database.clearTilesByStatus(2); // Clear player tiles
		database.clearTilesByStatus(3); // Clear random tiles
		database.clearTilesByStatus(4); // Clear bonus tiles
		database.setPlayerTiles(0, config.playerID());
		database.setRandomTiles(0);
		database.setBonusTiles(0);
		database.setStarted(1);
		//database.prestigeUpdate(prestigePoints, (int) client.getOverallExperience());

		WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
		if (currentLocation != null) {
			// Set current location and immediate surrounding tiles as unlocked (status 1)
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					WorldPoint tile = currentLocation.dx(dx).dy(dy);
					database.insertOrUpdateTile(tile.getX(), tile.getY(), tile.getPlane(), 4, 0); // Status 1 for unlocked
					addSideTilesAsUnlockable(tile);
				}
			}
		}
		unlockedTiles = database.getTilesByStatus(4);
		//reloadScene();
		if (!started) {
			started = true;
		}
	}

	private void addSideTilesAsUnlockable(WorldPoint currentLocation) {
		// Iterate through all neighbors (including diagonal)
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				// Skip the current tile
				if (dx == 0 && dy == 0) {
					continue;
				}
				int newX = currentLocation.getX() + dx;
				int newY = currentLocation.getY() + dy;
				int plane = currentLocation.getPlane();

				int check = database.getTileStatus(newX, newY, plane);
				// Check if the tile is neither unlocked nor already unlockable
				if (check == -1) {
					// Add the tile as unlockable in the database
					database.insertOrUpdateTile(newX, newY, plane, 1, 0); // Status 1 for unlockable
				}
			}
		}
		// Fetch the updated unlockable tiles list from the database
		unlockableTiles = database.getTilesByStatus(1); // Status 2 for player
	}

	public void loadTilesFromDatabase() {
		List<WorldPoint> newPlayerTiles = database.getTilesByStatus(2);
		List<WorldPoint> newRandomTiles = database.getTilesByStatus(3);
		List<WorldPoint> newBonusTiles = database.getTilesByStatus(4);
		List<WorldPoint> newUnlockableTiles = database.getTilesByStatus(1);

		// Clear current lists and update them with new data
		unlockedTiles.clear();
		unlockedTiles.addAll(newPlayerTiles);
		unlockedTiles.addAll(newRandomTiles);
		unlockedTiles.addAll(newBonusTiles);

		unlockableTiles.clear();
		unlockableTiles.addAll(newUnlockableTiles);
	}

	public int  playerTiles, randomTiles, bonusTiles, xpUntilNextPlayerTile, xpUntilNextRandomTile, prestigePoints;
	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		//updateAll();
	}
	public void updateAll() {
		prestigePoints = database.countTilesByStatus(3) + database.countTilesByStatus(2);
		double xpDiff = (int) (client.getOverallExperience() - database.getPrestigeXP(config.playerID()));
		int unlockablePlayerTiles = (int) Math.floor(xpDiff / config.XPForAPlayerTile());
		int unlockableRandomTiles = (int) Math.floor(xpDiff / config.XPForARandomTile());

		database.updateUserStats(getPlayerTiles(), getRandomTiles(), getBonusTiles(), getXPUntilNextPlayerTile(), (int) client.getOverallExperience(), config.playerID());
		playerTiles = unlockablePlayerTiles - database.countTilesByPlayerAndStatus(config.playerID(), 2);
		randomTiles = unlockableRandomTiles - database.countTilesByPlayerAndStatus(config.playerID(), 3);
		bonusTiles = database.countTilesByStatus(4);
		xpUntilNextPlayerTile = config.XPForAPlayerTile() - Integer.parseInt(Long.toString((client.getOverallExperience() - database.getPrestigeXP(config.playerID())) % config.XPForAPlayerTile()));
		xpUntilNextRandomTile = config.XPForARandomTile() - Integer.parseInt(Long.toString((client.getOverallExperience() - database.getPrestigeXP(config.playerID())) % config.XPForARandomTile()));
		if (xpDiff != 0) {
			if (database.countTilesByPlayerAndStatus(config.playerID(), 3) < unlockableRandomTiles) {
				WorldPoint unlocked = getRandomUnlockableTile();
				unlockRandomTile(unlocked, 3);
				addSideTilesAsUnlockable(unlocked);
				//prepareForTrouble();
				//reloadScene();
			}
		}
	}


	public void prepareForTrouble() {
		int currentUnlockPrepareForTrouble = database.getCurrentUnlocks("Prepare for trouble");
		int currentUnlockAndMakeItDouble = database.getCurrentUnlocks("Prepare for trouble");
		int chance = 0;

		if (currentUnlockPrepareForTrouble >= 1) {
			chance = currentUnlockPrepareForTrouble * 25;
		} else {
			return;
		}

		if (currentUnlockAndMakeItDouble == 1) {
			chance = chance * 2;
		}
		System.out.println("Chance: " + chance + "%");
		Random random = new Random();
		if (random.nextInt(100) < chance) { // Random roll to see if it's within the chance
			WorldPoint unlocked = getRandomUnlockableTile();
			if (unlocked != null) {
				unlockRandomTile(unlocked, 4);
				addSideTilesAsUnlockable(unlocked);
			}
		}
	}
	public int getPlayerTiles() {
		return playerTiles;
	}
	public int getRandomTiles() {
		return randomTiles;
	}

	public int getBonusTiles() {
		return bonusTiles;
	}
	public int getXPUntilNextPlayerTile() {
		return xpUntilNextPlayerTile;
	}
	public int getXPUntilNextRandomTile() {
		return xpUntilNextRandomTile;
	}
	public int getPrestigePoints() {
		return prestigePoints;
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		final boolean shiftPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
		final Tile selectedTile = client.getSelectedSceneTile();
		WorldPoint tileWorldPoint = selectedTile.getWorldLocation();
		MenuEntry[] menuEntries = client.getMenuEntries();
		WorldPoint clicked = new WorldPoint(tileWorldPoint.getX(), tileWorldPoint.getY(), 0 );


		if (started && !unlockedTiles.contains(clicked) && !unlockableTiles.contains(clicked)) {
			MenuEntry[] newEntries = Arrays.stream(menuEntries)
					.filter(entry -> !entry.getOption().equals("Walk here"))
					.toArray(MenuEntry[]::new);
			client.setMenuEntries(newEntries);

		}

		if (shiftPressed && event.getOption().equals("Walk here")) {
			/*
			if(unlockedTiles.contains(clicked)) {
				client.createMenuEntry(-1)
						.setOption("Remove Tile")
						.setTarget(event.getTarget())
						.setType(MenuAction.RUNELITE);
			} else */if(unlockableTiles.contains(clicked)) {
				client.createMenuEntry(-1)
						.setOption("Unlock Tile")
						.setTarget(event.getTarget())
						.setType(MenuAction.RUNELITE);
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (event.getMenuAction().getId() != MenuAction.RUNELITE.getId()) {
			return;
		}

		if (event.getMenuOption().equals("Unlock Tile") && playerTiles >= 1) {
			Tile target = client.getSelectedSceneTile();
			WorldPoint tileWorldPoint = target.getWorldLocation();
			if (tileWorldPoint == null) {
				return;
			}
			checkAndUnlockTile(tileWorldPoint);
		}
	}


	public int prevPrestigePoints = 0;
	@Subscribe
	public void onGameTick(GameTick gameTick) {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		updateAll();
		WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
		if(started) {
			if (!unlockedTiles.contains((new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0)))) {
				checkAndUnlockTile(currentLocation);

				removeTilesNotInList(unlockedTiles);
				lastPlayerLocation = currentLocation;
			}
			if (prestigePoints != prevPrestigePoints) {
				loadTilesFromDatabase();
				reloadScene();
				prevPrestigePoints = prestigePoints;
			}
		}
	}

	public void checkAndUnlockTile(WorldPoint currentLocation) {
		if (unlockableTiles.contains(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 )) && playerTiles >= 1) {
			// Unlock the tile
			unlockNewTile(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 ));

			// Update the status in the database to unlocked (status 2)
			database.insertOrUpdateTile(currentLocation.getX(), currentLocation.getY(), 0, 2, config.playerID());
			addSideTilesAsUnlockable(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 ));

			playerTiles -= 1;
			database.setPlayerTiles(-1, config.playerID());
		}
		if(!unlockedTiles.contains(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 )) && playerTiles >= 1) {
			// Unlock the tile
			unlockNewTile(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 ));

			// Update the status in the database to unlocked (status 2)
			database.insertOrUpdateTile(currentLocation.getX(), currentLocation.getY(), 0, 2, config.playerID());
			addSideTilesAsUnlockable(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 ));

			playerTiles -= 1;
			database.setPlayerTiles(-1, config.playerID());
		}
		long startTime = System.nanoTime();
		removeTilesNotInList(unlockedTiles);
		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		System.out.println("Function execution time: " + elapsedTime + " nanoseconds");

		//Scene();
	}

	private void unlockNewTile(WorldPoint tile) {
		if (!unlockedTiles.contains(tile)) {
			unlockedTiles.add(tile);
			unlockableTiles.remove(tile); // Remove from unlockable tiles if it's there
			// You can add more logic here if needed, e.g., saving to a file or handling UI updates
		}
	}

	public WorldPoint getRandomUnlockableTile() {
		if (unlockableTiles.isEmpty()) {
			return null; // Return null if there are no unlockable tiles
		}

		Random random = new Random();
		int randomIndex = random.nextInt(unlockableTiles.size());
		return unlockableTiles.get(randomIndex); // Return a random tile from the list
	}


	public void unlockRandomTile(WorldPoint tile, int tileStatus) {
		// Check if the tile exists in the database and is unlockable
		if (database.getTileStatus(tile.getX(), tile.getY(), tile.getPlane()) == 1) {
			// Update the status of the tile to 3 (randomly unlocked)
			database.insertOrUpdateTile(tile.getX(), tile.getY(), tile.getPlane(), tileStatus, config.playerID());
			unlockableTiles.remove(tile);
			unlockedTiles.add(tile);
			removeTilesNotInList(unlockedTiles);
			//reloadScene();
		}
	}


	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}

	public List<WorldPoint> getExcludedTiles() {
		return unlockedTiles;
	}

	public List<WorldPoint> getUnlockableTiles() {
		return unlockableTiles;
	}

	public Client getClient() {
		return client;
	}

	@Inject
	private ClientThread clientThread;



	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			if(started) {
				removeTilesNotInList(unlockedTiles);
			}
		}
	}

	public boolean unlockTilesEnabled = true;
	public void reloadScene() {
		if (unlockTilesEnabled) {
			clientThread.invokeLater(() -> {
				if (client.getGameState() == GameState.LOGGED_IN) {
					client.setGameState(GameState.LOADING);
				}
			});
		}
	}


	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(ExampleConfig.GROUP))
		{
			updateConfig();
		}
	}


	private boolean hideTiles;
	private boolean neightborTiles;

	private void updateConfig()
	{
		//hideTiles = config.hideTiles();
		neightborTiles = config.neighborTiles();
		if (neightborTiles) {
			overlayManager.add(unlockableTilesOverlay);
		}
		else {
			overlayManager.remove(unlockableTilesOverlay);
		}
		reloadScene();
	}

	public Integer checkStarted() {
		return database.getStarted(1);
	}

	public void removeTilesNotInList(List<WorldPoint> allowedTiles) {
		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		Set<WorldPoint> allowedSet = new HashSet<>(allowedTiles); // Convert to hash set for faster lookups
		Set<WorldPoint> unlockableSet = new HashSet<>(unlockableTiles);

		for (int z = 0; z < Constants.MAX_Z; z++) {
			boolean allRemoved = true; // Flag to track if all tiles on a plane are removed
			for (int x = 0; x < Constants.SCENE_SIZE; x++) {
				for (int y = 0; y < Constants.SCENE_SIZE; y++) {
					Tile tile = tiles[z][x][y];
					if (tile == null) continue;
					WorldPoint worldPoint = tile.getWorldLocation();
					if (allowedSet.contains(worldPoint)) {
						continue; // Skip allowed tiles
					}

					if (neightborTiles && unlockableSet.contains(worldPoint)) {
						continue; // Skip unlockable neighbor tiles
					}

					scene.removeTile(tile);
					allRemoved = false; // At least one tile removed
				}
				if (allRemoved) break; // Terminate loop if all tiles on the plane are removed
			}
		}
	}
}
