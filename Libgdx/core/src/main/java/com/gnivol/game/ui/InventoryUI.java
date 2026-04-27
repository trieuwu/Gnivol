package com.gnivol.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gnivol.game.system.inventory.InventoryManager;
import com.gnivol.game.system.inventory.CraftingManager;

public class InventoryUI {
    private Stage stage;
    private InventoryManager inventoryManager;
    private CraftingManager craftingManager;
    private Image highlight1;
    private Image highlight2;
    private Texture highlightTexture;

    private Table quickbarTable;
    private Table backpackTable;
    private Table topTable;

    private String selectedItem1 = null;
    private String selectedItem2 = null;
    private com.badlogic.gdx.utils.Array<ImageButton> backpackSlots = new com.badlogic.gdx.utils.Array<>();
    private com.badlogic.gdx.utils.Array<ImageButton> quickbarSlots = new com.badlogic.gdx.utils.Array<>();

    private Table tooltipTable;
    private com.badlogic.gdx.scenes.scene2d.ui.Label nameLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Label descLabel;
    private com.badlogic.gdx.utils.Timer.Task longPressTask;

    private com.badlogic.gdx.utils.ObjectMap<String, com.badlogic.gdx.utils.JsonValue> itemDatabase;
    private com.badlogic.gdx.graphics.g2d.BitmapFont font;
    private java.util.HashMap<String, Texture> itemTextureCache = new java.util.HashMap<>();

    private com.gnivol.game.system.rs.RSManager rsManager;
    private ImageButton useBtn;
    private ImageButton mergeBtn;
    private TextureRegionDrawable mergeNormalBg;
    private TextureRegionDrawable mergeGlitchBg;
    private TextureRegionDrawable useNormalBg;
    private TextureRegionDrawable useGlitchBg;

    public InventoryUI(Stage stage, InventoryManager inv, CraftingManager craft, com.gnivol.game.system.rs.RSManager rsManager, com.badlogic.gdx.graphics.g2d.BitmapFont font) {
        this.stage = stage;
        this.inventoryManager = inv;
        this.craftingManager = craft;
        this.rsManager = rsManager; // LƯU LẠI
        this.font = font;

        loadItemData();
        setupUI();
    }

    private void loadItemData() {
        itemDatabase = new com.badlogic.gdx.utils.ObjectMap<>();
        try {
            com.badlogic.gdx.utils.JsonReader jsonReader = new com.badlogic.gdx.utils.JsonReader();
            com.badlogic.gdx.utils.JsonValue base = jsonReader.parse(Gdx.files.internal("data/items.json"));
            for (com.badlogic.gdx.utils.JsonValue item : base.get("items")) {
                itemDatabase.put(item.getString("itemID"), item);
            }
        } catch (Exception e) {
            Gdx.app.error("InventoryUI", "Read Error", e);
        }
    }

    private void setupUI() {

        com.badlogic.gdx.graphics.Pixmap pixmapQuick = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapQuick.setColor(new Color(0, 0, 0, 0.4f));
        pixmapQuick.fill();
        TextureRegionDrawable quickBg = new TextureRegionDrawable(new TextureRegion(new Texture(pixmapQuick)));
        pixmapQuick.dispose();

        ImageButton.ImageButtonStyle slotStyleQuickbar = new ImageButton.ImageButtonStyle();
        slotStyleQuickbar.up = quickBg;

        com.badlogic.gdx.graphics.Pixmap pixmapTrans = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmapTrans.setColor(new Color(0, 0, 0, 0f));
        pixmapTrans.fill();
        TextureRegionDrawable transBg = new TextureRegionDrawable(new TextureRegion(new Texture(pixmapTrans)));
        pixmapTrans.dispose();

        ImageButton.ImageButtonStyle slotStyleBackpack = new ImageButton.ImageButtonStyle();
        slotStyleBackpack.up = transBg;

        topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().left().pad(20f);

        Texture baloTex = new Texture(Gdx.files.internal("images/UI/inventory_button.png"));
        ImageButton baloBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(baloTex)));
        baloBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isBackpackVisible = !backpackTable.isVisible();

                backpackTable.setVisible(isBackpackVisible);
                quickbarTable.setVisible(!isBackpackVisible);
                resetHighlights();
                if (!isBackpackVisible) {
                    resetHighlights();
                } else {

                    updateButtonStates();
                }
            }
        });
        Texture baloFrameTex = new Texture(Gdx.files.internal("images/UI/item_frame.png"));
        itemTextureCache.put("balo_frame_cache", baloFrameTex);

        Table baloWrapper = new Table();
        baloWrapper.setBackground(new TextureRegionDrawable(new TextureRegion(baloFrameTex)));

        baloWrapper.add(baloBtn).size(110, 110).center();
        topTable.add(baloWrapper).size(80, 80);

        quickbarTable = new Table();
        quickbarTable.setFillParent(true);
        quickbarTable.bottom().padBottom(15f);

        for (int i = 0; i < 5; i++) {
            final int index = i;
            final ImageButton quickbarSlotBtn = new ImageButton(slotStyleQuickbar);

            quickbarSlotBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleSlotClick(index, quickbarSlotBtn, true);
                }
            });

            quickbarTable.add(quickbarSlotBtn).size(80, 80).pad(8f);
            quickbarSlots.add(quickbarSlotBtn);
        }


        backpackTable = new Table();
        backpackTable.setFillParent(true);
        backpackTable.center();

        Texture bgTex = new Texture(Gdx.files.internal("images/UI/inventory_chart_ui.png"));
        backpackTable.setBackground(new TextureRegionDrawable(new TextureRegion(bgTex)));


        Table gridTable = new Table();


        for (int i = 0; i < 25; i++) {
            final int index = i;
            final ImageButton slotBtn = new ImageButton(slotStyleBackpack);

            slotBtn.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    final String itemId = (index < inventoryManager.getItems().size()) ? inventoryManager.getItems().get(index) : null;
                    if (itemId == null) return false;

                    if (button == com.badlogic.gdx.Input.Buttons.RIGHT) {
                        Vector2 stagePos = slotBtn.localToStageCoordinates(new Vector2(x, y));
                        showTooltip(itemId, stagePos.x, stagePos.y);
                        return true;
                    }

                    if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
                        longPressTask = com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                            @Override
                            public void run() {
                                Vector2 stagePos = slotBtn.localToStageCoordinates(new Vector2(x, y));
                                showTooltip(itemId, stagePos.x, stagePos.y);
                            }
                        }, 0.5f);
                    }
                    return true;
                }

                @Override
                public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    hideTooltip();


                    if (button == com.badlogic.gdx.Input.Buttons.LEFT && (longPressTask == null || !longPressTask.isScheduled())) {

                        handleSlotClick(index, slotBtn, false);
                    }
                }

                @Override
                public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    hideTooltip();
                }
            });
            gridTable.add(slotBtn).size(65, 65).pad(10f);
            backpackSlots.add(slotBtn);

            if ((i + 1) % 5 == 0) gridTable.row();
        }


        backpackTable.add(gridTable).row();


        mergeNormalBg = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/UI/Merge_button.png"))));
        mergeGlitchBg = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/UI/Merge_button_glitch.png"))));
        useNormalBg = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/UI/use_button.png"))));
        useGlitchBg = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/UI/use_button_glitch.png"))));

        useBtn = new ImageButton(useNormalBg);
        useBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("InventoryUI", "Nút USE được bấm (Chưa có chức năng)");
            }
        });

        mergeBtn = new ImageButton(mergeNormalBg);
        mergeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                attemptCraft();
            }
        });

        com.badlogic.gdx.scenes.scene2d.ui.Stack overlayStack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        Table gridLayer = new Table();
        gridLayer.add(gridTable).center();
        overlayStack.add(gridLayer);

        Table actionLayer = new Table();
        actionLayer.bottom().padBottom(30f);
        actionLayer.add(useBtn).size(280, 140).padRight(-50f);
        actionLayer.add(mergeBtn).size(280, 140);
        overlayStack.add(actionLayer);

        backpackTable.add(overlayStack).expand().fill();

        backpackTable.setVisible(false);
        stage.addActor(backpackTable);

        stage.addActor(quickbarTable);

        stage.addActor(topTable);

        highlightTexture = new Texture(Gdx.files.internal("images/UI/item_frame.png"));

        highlight1 = new Image(highlightTexture);
        highlight1.setSize(95, 95);
        highlight1.setVisible(false);
        highlight1.setTouchable(Touchable.disabled);

        highlight2 = new Image(highlightTexture);
        highlight2.setSize(95, 95);
        highlight2.setColor(Color.CYAN);
        highlight2.setVisible(false);
        highlight2.setTouchable(Touchable.disabled);

        stage.addActor(highlight1);
        stage.addActor(highlight2);

        backpackTable.setTouchable(Touchable.enabled);
        backpackTable.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle ttNameStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(this.font, Color.YELLOW);
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle ttDescStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(this.font, Color.WHITE);

        nameLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("", ttNameStyle);
        descLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("", ttDescStyle);
        descLabel.setWrap(true);

        tooltipTable = new Table();

        com.badlogic.gdx.graphics.Pixmap ttPixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        ttPixmap.setColor(new Color(0, 0, 0, 0.85f));
        ttPixmap.fill();
        tooltipTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(ttPixmap))));
        ttPixmap.dispose();

        tooltipTable.add(nameLabel).left().row();
        tooltipTable.add(descLabel).width(200f).left();
        tooltipTable.pad(10f);
        tooltipTable.setVisible(false);
        tooltipTable.setTouchable(Touchable.disabled);

        stage.addActor(tooltipTable);

    }

    private void showTooltip(String itemId, float screenX, float screenY) {
        if (itemId == null) return;

        com.badlogic.gdx.utils.JsonValue itemData = itemDatabase.get(itemId);
        String name = itemData != null ? itemData.getString("itemName", "Secret item") : "Secret item";
        String desc = itemData != null ? itemData.getString("description", "No infomation") : "No information";

        nameLabel.setText(name);
        descLabel.setText(desc);

        tooltipTable.pack();

        tooltipTable.setPosition(screenX + 15, screenY - tooltipTable.getHeight() - 10);
        tooltipTable.setVisible(true);
        tooltipTable.toFront();
    }

    private void hideTooltip() {
        tooltipTable.setVisible(false);
        if (longPressTask != null) longPressTask.cancel();
    }

    private void attemptCraft() {
        if (selectedItem1 != null && selectedItem2 != null) {
            String result = craftingManager.getMergeResult(selectedItem1, selectedItem2);
            if (result != null) {
                Gdx.app.log("Crafting", "Success: " + result);
                inventoryManager.removeItem(selectedItem1);
                inventoryManager.removeItem(selectedItem2);
                inventoryManager.addItem(result);

                String itemName = result;
                com.badlogic.gdx.utils.JsonValue itemData = itemDatabase.get(result);
                if (itemData != null && itemData.getString("itemName", null) != null) {
                    itemName = itemData.getString("itemName");
                }
                showNotification("Created: " + itemName, Color.GREEN);

                refreshUI();
                resetHighlights();
            } else {
                Gdx.app.log("Crafting", "Failed: No valid combination");
                showNotification("Merge Failed", Color.RED);
                resetHighlights();
            }

            selectedItem1 = null;
            selectedItem2 = null;
        }
    }

    private void handleSlotClick(int slotIndex, ImageButton clickedBtn, boolean isQuickbar) {
        java.util.ArrayList<String> currentItems = inventoryManager.getItems();

        if (slotIndex >= currentItems.size()) {
            resetHighlights();
            return;
        }

        String itemId = currentItems.get(slotIndex);
        Vector2 btnPos = new Vector2(0, 0);
        clickedBtn.localToStageCoordinates(btnPos);

        float slotWidth = clickedBtn.getWidth();
        float slotHeight = clickedBtn.getHeight();

        float expandSize = 14f; // Tăng thêm 14 pixel (Bạn có thể sửa số này cho vừa mắt)
        float newWidth = slotWidth + expandSize;
        float newHeight = slotHeight + expandSize;
        // Dịch tọa độ X, Y lùi lại một nửa độ nở để khung luôn căn giữa
        float newX = btnPos.x - (7f);
        float newY = btnPos.y - (7f);

        if (isQuickbar) {
            if (selectedItem1 != null && selectedItem1.equals(itemId) && highlight1.isVisible()) {
                resetHighlights();
            }
            else {
                resetHighlights();
                selectedItem1 = itemId;

                highlight1.setSize(newWidth, newHeight);
                highlight1.setPosition(newX, newY);
                highlight1.setVisible(true);
                highlight1.clearActions();
                highlight1.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.5f, 0.5f), Actions.alpha(1f, 0.5f))));
            }
            return;
        }

        if (selectedItem1 == null) {
            selectedItem1 = itemId;

            highlight1.setSize(newWidth+2, newHeight+7);
            highlight1.setPosition(newX, newY+2);
            highlight1.setVisible(true);
            highlight1.clearActions();
            highlight1.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.5f, 0.5f), Actions.alpha(1f, 0.5f))));
        } else if (selectedItem1.equals(itemId) && selectedItem2 == null) {
            resetHighlights();
        } else if (selectedItem2 == null) {
            selectedItem2 = itemId;

            highlight2.setSize(newWidth+2, newHeight+7);
            highlight2.setPosition(newX, newY+2);
            highlight2.setVisible(true);
            highlight2.clearActions();
            highlight2.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.5f, 0.5f), Actions.alpha(1f, 0.5f))));
        } else {
            resetHighlights();
        }
    }
    private void resetHighlights() {
        selectedItem1 = null;
        selectedItem2 = null;
        highlight1.setVisible(false);
        highlight1.clearActions();
        highlight1.getColor().a = 1f;
        highlight2.setVisible(false);
        highlight2.clearActions();
        highlight2.getColor().a = 1f;
    }

    public void refreshUI() {
        java.util.ArrayList<String> items = inventoryManager.getItems();

        for (int i = 0; i < 25; i++) {
            ImageButton slot = backpackSlots.get(i);
            updateSlotVisual(slot, i < items.size() ? items.get(i) : null);
        }

        for (int i = 0; i < 5; i++) {
            ImageButton slot = quickbarSlots.get(i);
            updateSlotVisual(slot, i < items.size() ? items.get(i) : null);
        }
    }

    private void updateSlotVisual(ImageButton slot, String itemID) {

        slot.clearChildren();

        if (itemID != null) {
            try {
                if (!itemTextureCache.containsKey(itemID)) {
                    Texture tex = new Texture(Gdx.files.internal("images/item/" + itemID + ".png"));
                    itemTextureCache.put(itemID, tex);
                }
                Image icon = new Image(itemTextureCache.get(itemID));
                icon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
                icon.setAlign(com.badlogic.gdx.utils.Align.center);
                slot.add(icon).expand().fill();
            } catch (Exception e) {
                Gdx.app.error("InventoryUI", "No image" + itemID + ".png");
            }
        }
    }
    public boolean isOpen() {
        return backpackTable != null && backpackTable.isVisible();
    }

    private void showNotification(String text, Color color) {
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle notifStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(font, color);
        com.badlogic.gdx.scenes.scene2d.ui.Label notifLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label(text, notifStyle);

        notifLabel.setPosition((1280 - notifLabel.getPrefWidth()) / 2f, 6f);
        notifLabel.getColor().a = 0f;

        stage.addActor(notifLabel);

        notifLabel.addAction(Actions.sequence(
            Actions.parallel(Actions.fadeIn(1f), Actions.moveBy(0, 30f, 1f)),
            Actions.delay(3f),
            Actions.parallel(Actions.fadeOut(1.5f), Actions.moveBy(0, -30f, 1.5f)),
            Actions.removeActor()
        ));
    }
    public void setVisible(boolean visible) {
        if (topTable != null) topTable.setVisible(visible);
        if (!visible) {
            resetHighlights();
            if (quickbarTable != null) quickbarTable.setVisible(false);
            if (backpackTable != null && backpackTable.isVisible()) {
                backpackTable.setVisible(false);
                resetHighlights();
            }
        } else {
            if (backpackTable != null && backpackTable.isVisible()) {
                if (quickbarTable != null) quickbarTable.setVisible(false);
            } else {
                if (quickbarTable != null) quickbarTable.setVisible(true);
            }
        }
    }

    public String getSelectedItem() {
        if (backpackTable != null && !backpackTable.isVisible()) {
            return selectedItem1;
        }
        return null;
    }

    public void clearSelection() {
        resetHighlights();
    }

    public void updateButtonStates() {
        if (rsManager == null) return;

        boolean isGlitch = rsManager.isAboveThreshold();

        if (isGlitch) {
            useBtn.getStyle().imageUp = useGlitchBg;
            mergeBtn.getStyle().imageUp = mergeGlitchBg;
        } else {
            useBtn.getStyle().imageUp = useNormalBg;
            mergeBtn.getStyle().imageUp = mergeNormalBg;
        }
    }

    public void dispose() {
        for (Texture tex : itemTextureCache.values()) {
            tex.dispose();
        }
        itemTextureCache.clear();
    }
}

