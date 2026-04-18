package com.gnivol.game;

public final class Constants {

    // Window
    public static final String GAME_TITLE = "Gnivol";
    public static final String GAME_VERSION = "1.0.3";
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final int TARGET_FPS = 60;

    // World / Camera
    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    // Reality Stability (RS)
    public static final float RS_DEFAULT = 100f;
    public static final float RS_MIN = 0f;
    public static final float RS_MAX = 100f;
    public static final float RS_GLITCH_THRESHOLD = 40f;
    public static final float RS_HORROR_THRESHOLD = 30f;
    public static final float RS_META_THRESHOLD = 80f;

    // Inventory
    public static final int INVENTORY_MAX_SLOTS = 20;

    // Save
    public static final String SAVE_FILE_NAME = "save.json";
    public static final float AUTO_SAVE_INTERVAL = 5f;

    // Scene IDs
    public static final String SCENE_BEDROOM = "room_bedroom";
    public static final String SCENE_BATHROOM = "room_bathroom";

    // Asset paths
    public static final String ASSETS_DATA = "data/";
    public static final String ASSETS_ROOMS = "data/rooms/";
    public static final String ASSETS_DIALOGUE = "data/dialogue/";
    public static final String ASSETS_AUDIO = "audio/";
    public static final String ASSETS_TEXTURES = "textures/";
    public static final String ASSETS_BACKGROUNDS = "textures/backgrounds/";
    public static final String ASSETS_OBJECTS = "textures/objects/";
    public static final String ASSETS_CHARACTERS = "textures/characters/";
    public static final String DATA_ITEMS = "data/items.json";
    public static final int MAX_INVENTORY_SLOTS = 25;

    private Constants() {}
}
