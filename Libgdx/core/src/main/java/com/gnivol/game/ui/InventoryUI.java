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

    private String selectedItem1 = null;
    private String selectedItem2 = null;
    private com.badlogic.gdx.utils.Array<ImageButton> backpackSlots = new com.badlogic.gdx.utils.Array<>();
    private com.badlogic.gdx.utils.Array<ImageButton> quickbarSlots = new com.badlogic.gdx.utils.Array<>();

    public InventoryUI(Stage stage, InventoryManager inv, CraftingManager craft) {
        this.stage = stage;
        this.inventoryManager = inv;
        this.craftingManager = craft;
        setupUI();
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

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().left().pad(20f);

        Texture baloTex = new Texture(Gdx.files.internal("images/inventory_button.png"));
        ImageButton baloBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(baloTex)));
        baloBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isBackpackVisible = !backpackTable.isVisible();
                backpackTable.setVisible(isBackpackVisible);

                quickbarTable.setVisible(!isBackpackVisible);
                if (!isBackpackVisible) {
                    resetHighlights();
                }
            }
        });
        topTable.add(baloBtn).size(150, 150);

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

        Texture bgTex = new Texture(Gdx.files.internal("images/inventory_chart_ui.png"));
        backpackTable.setBackground(new TextureRegionDrawable(new TextureRegion(bgTex)));


        Table gridTable = new Table();


        for (int i = 0; i < 25; i++) {
            final int index = i;
            final ImageButton slotBtn = new ImageButton(slotStyleBackpack);

            slotBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleSlotClick(index, slotBtn, false);
                }
            });
            gridTable.add(slotBtn).size(65, 65).pad(10f);
            backpackSlots.add(slotBtn);

            if ((i + 1) % 5 == 0) gridTable.row();
        }


        backpackTable.add(gridTable).row();


        Texture mergeTex = new Texture(Gdx.files.internal("images/Merge_button.png"));
        ImageButton mergeBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(mergeTex)));
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

        Table mergeLayer = new Table();
        mergeLayer.bottom().padBottom(40f); // Tăng giảm số này để đẩy nút lên/xuống
        mergeLayer.add(mergeBtn).size(450, 150);
        overlayStack.add(mergeLayer);

        backpackTable.add(overlayStack).expand().fill();

        backpackTable.setVisible(false);
        stage.addActor(backpackTable);

        stage.addActor(quickbarTable);

        stage.addActor(topTable);

        highlightTexture = new Texture(Gdx.files.internal("images/thinking_final.png"));

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
                // Trả về true để báo với Stage rằng sự kiện đã được xử lý ở đây
                return true;
            }
        });
    }
    private void attemptCraft() {
        if (selectedItem1 != null && selectedItem2 != null) {
            String result = craftingManager.getMergeResult(selectedItem1, selectedItem2);
            if (result != null) {
                Gdx.app.log("Crafting", "Success: " + result);
                inventoryManager.removeItem(selectedItem1);
                inventoryManager.removeItem(selectedItem2);
                inventoryManager.addItem(result);

                refreshUI();
                resetHighlights();
            } else {
                Gdx.app.log("Crafting", "Failed: No valid combination");
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

            if (isQuickbar) {
                if (selectedItem1 != null && selectedItem1.equals(itemId) && highlight1.isVisible()) {
                    resetHighlights();
                }
                else {
                    resetHighlights();
                    selectedItem1 = itemId;
                    highlight1.setPosition(btnPos.x - 5, btnPos.y - 5);
                    highlight1.setVisible(true);
                    highlight1.clearActions();
                    highlight1.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.5f, 0.5f), Actions.alpha(1f, 0.5f))));
                }
                return;
            }

            if (selectedItem1 == null) {
                selectedItem1 = itemId;
                highlight1.setPosition(btnPos.x - 5, btnPos.y - 5); // Căn chỉnh cho khớp viền
                highlight1.setVisible(true);

                highlight1.clearActions();
                highlight1.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.5f, 0.5f), Actions.alpha(1f, 0.5f))));
            } else if (selectedItem1.equals(itemId) && selectedItem2 == null) {
                resetHighlights();
            } else if (selectedItem2 == null) {
                selectedItem2 = itemId;
                highlight2.setPosition(btnPos.x - 5, btnPos.y - 5);
                highlight2.setVisible(true);
            } else {
                resetHighlights();
            }
        }
        private void resetHighlights() {
            selectedItem1 = null;
            selectedItem2 = null;
            highlight1.setVisible(false);
            highlight1.clearActions();
            highlight2.setVisible(false);
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
                Texture itemTex = new Texture(Gdx.files.internal("images/item/" + itemID + ".png"));
                Image icon = new Image(itemTex);


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
}


