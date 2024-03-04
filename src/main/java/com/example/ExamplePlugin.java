package com.example;

import com.example.SQLite.Database;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
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

		//unlockRandomTilesInArea(20000);
	}

	public void unlockRandomTilesInArea(int numTiles) {
		//WorldArea area = new WorldArea(1029, 2505, 2933, 1649, 0);
		WorldArea area = new WorldArea(1907, 2777, 1927, 1121, 0);

		int minX = area.getX();
		int minY = area.getY();
		int maxX = minX + area.getWidth();
		int maxY = minY + area.getHeight();

		List<WorldPoint> randomTiles = new ArrayList<>();
		Random random = new Random();

		// Generate 10,000 random tiles within the area
		while (randomTiles.size() < numTiles) {
			int x = random.nextInt(maxX - minX + 1) + minX;
			int y = random.nextInt(maxY - minY + 1) + minY;
			WorldPoint tile = new WorldPoint(x, y, 0);
			if (area.contains(tile)) {
				randomTiles.add(tile);
			}
		}

		// Unlock the random tiles and their side tiles
		int i = 0;
		for (WorldPoint tile : randomTiles) {
			unlockRandomTileTest(tile, 3); // tileStatus 3 is randomTiles I think
			System.out.println("Added tile: " + tile + " " + i);
			i += 1;
			addSideTilesAsUnlockable(tile); // Assuming addSideTilesAsUnlockable is your function
		}
	}

	public void unlockRandomTileTest(WorldPoint tile, int tileStatus) {
		database.insertOrUpdateTile(tile.getX(), tile.getY(), tile.getPlane(), tileStatus, config.playerID());
			//unlockableTiles.remove(tile);
		unlockedTiles.add(tile);
			//removeTilesNotInList(unlockedTiles);
			//reloadScene();
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
		database.setStarted(1);
		WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
		if (currentLocation != null) {
			database.insertOrUpdateTile(currentLocation.getX(), currentLocation.getY(), currentLocation.getPlane(), 4, 0); // Status 1 for unlocked
			addSideTilesAsUnlockable(currentLocation);
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

	public int  playerTiles, randomTiles, bonusTiles, xpUntilNextPlayerTile, xpUntilNextRandomTile, prestigePoints, totalTiles;
	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		updateOverlayStats();
	}

	public void updateOverlayStats() {
		long startTime = System.nanoTime();
		double xpDiff = (int) client.getOverallExperience();
		int unlockablePlayerTiles = (int) Math.floor(xpDiff / config.XPForAPlayerTile());
		int unlockableRandomTiles = (int) Math.floor(xpDiff / config.XPForARandomTile());

		//playerTiles
		playerTiles = unlockablePlayerTiles - database.getPlayerTiles(config.playerID());
		randomTiles = unlockableRandomTiles - database.getRandomTiles(config.playerID());

		//nextPlayerTile
		xpUntilNextPlayerTile = config.XPForAPlayerTile() - Integer.parseInt(Long.toString((client.getOverallExperience()) % config.XPForAPlayerTile()));
		//nextRandomTile
		xpUntilNextRandomTile = config.XPForARandomTile() - Integer.parseInt(Long.toString((client.getOverallExperience()) % config.XPForARandomTile()));

		//TotalTiles
		totalTiles = unlockedTiles.size();

		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		System.out.println("Function execution time: " + elapsedTime + " nanoseconds " + unlockedTiles.size());
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
	public int getTotalTiles() {
		return totalTiles;
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
			if(unlockableTiles.contains(clicked)) {
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

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
		if(started) {
			int databaseTotalTiles = database.getNumberOfTiles();
			int localTotalTiles = unlockedTiles.size() + unlockableTiles.size();
			if (!unlockedTiles.contains((new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0)))) {
				checkAndUnlockTile(currentLocation);

				removeTilesNotInList(unlockedTiles);
				lastPlayerLocation = currentLocation;
			}
			if (randomTiles > database.getRandomTiles(config.playerID())) {
				System.out.println("Unlocking random tile");
				WorldPoint unlocked = getRandomUnlockableTile();
				unlockRandomTile(unlocked, 3);
				addSideTilesAsUnlockable(unlocked);
			}
			if (databaseTotalTiles != localTotalTiles) {
				System.out.println("Updating tiles from db");
				loadTilesFromDatabase();
				reloadScene();
				updateOverlayStats();
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

			//playerTiles += 1;
			database.setPlayerTiles(1, config.playerID());
			updateOverlayStats();
			reloadScene();
		}
		/*
		if(!unlockedTiles.contains(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 )) && playerTiles >= 1) {
			// Unlock the tile
			unlockNewTile(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 ));

			// Update the status in the database to unlocked (status 2)
			database.insertOrUpdateTile(currentLocation.getX(), currentLocation.getY(), 0, 2, config.playerID());
			addSideTilesAsUnlockable(new WorldPoint(currentLocation.getX(), currentLocation.getY(), 0 ));

			//playerTiles += 1;
			database.setPlayerTiles(1, config.playerID());
			System.out.println("!unlockedTiles");
		}

		 */
		long startTime = System.nanoTime();
		removeTilesNotInList(unlockedTiles);
		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		//System.out.println("Function execution time: " + elapsedTime + " nanoseconds");

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
		Scene scene = client.getScene();

		if (database.getTileStatus(tile.getX(), tile.getY(), tile.getPlane()) == 1) {
			// Update the status of the tile to 3 (randomly unlocked)
			database.insertOrUpdateTile(tile.getX(), tile.getY(), tile.getPlane(), tileStatus, config.playerID());
			database.setRandomTiles(config.playerID());
			unlockableTiles.remove(tile);
			unlockedTiles.add(tile);
			removeTilesNotInList(unlockedTiles);
			//reloadScene();
			if(WorldPoint.isInScene(scene, tile.getX(), tile.getY())) {
				reloadScene();
			}
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
				setViewableUnlockableTiles();
				System.out.println(setViewableUnlockableTiles().size());
				System.out.println(unlockableTiles.size());
			}
		}
	}

	public static List<WorldPoint> viewableUnlockableTiles = new ArrayList<>();
	public List<WorldPoint> getViewableUnlockableTiles() {
		return viewableUnlockableTiles;
	}
	public List<WorldPoint> setViewableUnlockableTiles() {
		//List<WorldPoint> viewableTiles = new ArrayList<>();
		viewableUnlockableTiles.clear();
		// Get the currently loaded scene
		Scene scene = client.getScene();

		// Iterate through the tiles in getRegionUnlockableTiles
		for (WorldPoint tile : getUnlockableTiles()) {
			// Check if the tile is present in the loaded scene
			if (WorldPoint.isInScene(scene, tile.getX(), tile.getY())) {
				viewableUnlockableTiles.add(tile);
			}
		}

		return viewableUnlockableTiles;
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

	private boolean neightborTiles;

	private void updateConfig()
	{
		//hideTiles = config.hideTiles();
		neightborTiles = config.neighborTiles();
		if (neightborTiles) {
			overlayManager.add(unlockableTilesOverlay);
			reloadScene();
		}
		else {
			overlayManager.remove(unlockableTilesOverlay);
			reloadScene();
		}
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
