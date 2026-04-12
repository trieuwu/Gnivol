package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.input.InputHandler;
import com.gnivol.game.system.scene.SceneController;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.system.scene.ScreenFader;

/**
 * Màn hình chơi game chính.
 *
 * Sau refactor: GameScreen chỉ lo RENDER + INPUT.
 * Toàn bộ logic tương tác, overlay, chuyển cảnh do SceneController điều phối.
 *
 * GameScreen implement SceneController.ViewListener để nhận thông báo
 * khi nào cần hiện text, mở overlay, phát sound, v.v.
 */
public class GameScreen extends BaseScreen implements SceneController.ViewListener {

    // SpriteBatch: dùng để vẽ texture (background, objects, overlay) lên màn hình
    private SpriteBatch batch;

    // SceneManager: quản lý phòng hiện tại, update + render scene
    private SceneManager sceneManager;

    // ScreenFader: hiệu ứng fade đen khi chuyển phòng
    private ScreenFader screenFader;

    // InputHandler: quản lý input (chuột, phím), multiplexer nhiều processor
    private InputHandler inputHandler;

    // Bộ điều phối logic
    private SceneController sceneController;

    // true = lần đầu vào GameScreen, cần load phòng ngủ
    // false = resume từ PauseScreen, không load lại
    private boolean firstShow = true;

    // inspectLabel: Label hiện text mô tả khi click object
    private Label inspectLabel;

    // inspectTable: Table chứa inspectLabel, đặt ở đáy màn hình
    private Table inspectTable;

    // vietnameseFont: font chữ hỗ trợ tiếng Việt có dấu
    private BitmapFont vietnameseFont;

    // fontGenerator: FreeType generator để tạo BitmapFont từ file .ttf
    private FreeTypeFontGenerator fontGenerator;

    // overlayTexture: ảnh overlay đang hiện (VD: ảnh tủ mở), null nếu không có overlay
    private Texture overlayTexture;

    // dimRenderer: vẽ hình chữ nhật đen bán trong suốt phủ lên scene khi mở overlay
    private ShapeRenderer dimRenderer;

    // Chuỗi chứa TẤT CẢ ký tự tiếng Việt (hoa + thường + dấu)
    // FreeType cần biết trước ký tự nào sẽ dùng để tạo bitmap font
    private static final String VIETNAMESE_CHARS =
            "aăâbcdđeêfghijklmnoôơpqrstuưvwxyz"
                    + "AĂÂBCDĐEÊFGHIJKLMNOÔƠPQRSTUƯVWXYZ"
                    + "àáảãạằắẳẵặầấẩẫậèéẻẽẹềếểễệìíỉĩịòóỏõọồốổỗộờớởỡợùú��ũụừứửữựỳýỷỹỵ"
                    + "ÀÁẢÃẠẰẮẲẴẶẦẤẨẪẬÈÉẺẼẸỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌỒỐỔỖỘỜỚỞỠỢÙÚỦŨỤỪỨỬỮỰỲÝỶỸỴ"
                    + "0123456789.,;:!?'\"-()[]{}…—–/\\@#$%^&*+=<>~`| ";

    /**
     * Constructor: nhận GnivolGame (lớp chính) để lấy các manager dùng chung.
     */
    public GameScreen(GnivolGame game) {
        super(game); // BaseScreen tạo camera + viewport (1280x720)
    }

    // =========================================================================
    // SHOW - gọi 1 lần khi chuyển sang GameScreen
    // =========================================================================

    @Override
    public void show() {
        // --- Lấy reference các manager từ GnivolGame ---
        sceneManager = game.getSceneManager();       // quản lý phòng
        screenFader = game.getScreenFader();          // hiệu ứng fade
        inputHandler = game.getInputHandler();        // quản lý input
        sceneController = game.getSceneController();  // bộ điều phối logic (MỚI)

        // Tạo SpriteBatch để vẽ texture
        batch = new SpriteBatch();

        // Tạo ShapeRenderer để vẽ nền mờ đen cho overlay
        dimRenderer = new ShapeRenderer();

        // Đăng ký GameScreen làm ViewListener cho SceneController
        // Từ giờ SceneController gọi viewListener.onShowInspectText()
        // → chính là GameScreen.onShowInspectText()
        sceneController.setViewListener(this);

        // --- Tạo font tiếng Việt từ file .ttf ---
        // Load file font arial.ttf
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));

        // Cấu hình font
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 22;                                                    // cỡ chữ 22px
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS; // ký tự cần render
        param.color = Color.WHITE;                                          // màu chữ trắng
        param.borderWidth = 1.5f;                                           // viền đen dày 1.5px
        param.borderColor = Color.BLACK;                                    // màu viền đen

        // Tạo BitmapFont từ cấu hình trên
        vietnameseFont = fontGenerator.generateFont(param);

        // --- Tạo UI inspect text (Label + Table) ---

        // Tạo style cho Label: dùng font tiếng Việt, màu trắng
        Label.LabelStyle labelStyle = new Label.LabelStyle(vietnameseFont, Color.WHITE);

        // Tạo Label rỗng (text sẽ ��ược set sau khi click object)
        inspectLabel = new Label("", labelStyle);
        inspectLabel.setWrap(true);              // tự xuống dòng khi text dài
        inspectLabel.setAlignment(Align.center); // căn giữa text

        // Tạo Table chứa Label — Table giúp căn vị trí dễ dàng
        inspectTable = new Table();
        inspectTable.setFillParent(true);        // Table chiếm toàn bộ Stage
        inspectTable.bottom().padBottom(30f);    // Label nằm ở đáy, cách đáy 30px
        inspectTable.add(inspectLabel).width(900f).pad(15f); // Label rộng 900px, padding 15px
        inspectTable.setVisible(false);          // Ban đầu ẩn — chỉ hiện khi click object

        // Thêm Table vào Stage (Scene2D UI system)
        game.getStage().addActor(inspectTable);

        // --- Đăng ký input: chuyển MỌI click cho SceneController ---

        // Xóa input processor cũ (nếu có từ screen trước)
        inputHandler.clear();

        // Thêm Stage vào input (để Scene2D UI nhận click/touch)
        inputHandler.addStage(game.getStage());

        // Thêm processor xử lý click chuột và phím
        inputHandler.addProcessor(new InputAdapter() {
            /**
             * Khi click chuột: chuyển hết cho SceneController.
             * TRƯỚC refactor: GameScreen tự kiểm tra overlay, tự gọi interactionSystem.
             * SAU refactor: SceneController.handleClick() lo toàn bộ.
             */
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return sceneController.handleClick(screenX, screenY, viewport);
            }

            /**
             * Khi nhấn ESC:
             * 1. Overlay đang mở → đóng overlay
             * 2. Overlay không mở → mở PauseScreen
             */
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (sceneController.isOverlayActive()) {
                        // Overlay đang mở → đóng overlay
                        sceneController.handleClick(0, 0, viewport);
                    } else {
                        // Không có overlay → mở PauseScreen
                        // "this" ở đây là InputAdapter, không phải GameScreen
                        // Nên dùng "GameScreen.this" để trỏ đến GameScreen bên ngoài
                        game.setScreen(new PauseScreen(game, GameScreen.this));
                    }
                    return true;
                }
                return false;
            }
        });

        // Kích hoạt input (set InputMultiplexer cho Gdx.input)
        inputHandler.activate();

        // --- Load phòng đầu tiên (chỉ lần đầu, không load lại khi resume) ---
        if (firstShow) {
            sceneManager.changeScene(Constants.SCENE_BEDROOM);
            screenFader.startFadeIn();
            firstShow = false;
            Gdx.app.log("GameScreen", "Game started");
        } else {
            Gdx.app.log("GameScreen", "Resumed from pause");
        }
    }

    // =========================================================================
    // VIEW LISTENER — 5 method nhận thông báo từ SceneController
    // GameScreen chỉ xử lý phần RENDER/SOUND, không có logic game
    // =========================================================================

    /**
     * SceneController bảo hiện inspect text.
     * GameScreen: set text vào Label + chạy animation fade-in 0.3 giây.
     */
    @Override
    public void onShowInspectText(String text) {
        // Set nội dung text mới
        inspectLabel.setText(text);

        // Hiện Table
        inspectTable.setVisible(true);

        // Bắt đầu từ trong suốt (alpha = 0)
        inspectTable.getColor().a = 0f;

        // Chạy animation fade-in trong 0.3 giây (alpha 0 → 1)
        inspectTable.addAction(Actions.fadeIn(0.3f));
    }

    /**
     * SceneController bảo ẩn inspect text.
     * GameScreen: chạy animation fade-out 0.3 giây, rồi ẩn Table.
     */
    @Override
    public void onHideInspectText() {
        // Chỉ ẩn nếu đang hiện
        if (inspectTable.isVisible()) {
            // Chạy 2 action nối tiếp: fade-out → ẩn
            inspectTable.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),    // alpha 1 → 0 trong 0.3 giây
                    Actions.visible(false)    // sau đó ẩn Table hoàn toàn
            ));
        }
    }

    /**
     * SceneController bảo mở overlay.
     * GameScreen: load texture từ file vào bộ nhớ GPU.
     */
    @Override
    public void onOpenOverlay(String texturePath) {
        try {
            // Load file ảnh thành Texture (nằm trong bộ nhớ GPU)
            overlayTexture = new Texture(Gdx.files.internal(texturePath));
            Gdx.app.log("GameScreen", "Overlay texture loaded: " + texturePath);
        } catch (Exception e) {
            // File không tồn tại hoặc lỗi → log lỗi, không crash game
            Gdx.app.error("GameScreen", "Cannot load overlay: " + texturePath, e);
        }
    }

    /**
     * SceneController bảo đóng overlay.
     * GameScreen: dispose texture (giải phóng bộ nhớ GPU).
     */
    @Override
    public void onCloseOverlay() {
        if (overlayTexture != null) {
            overlayTexture.dispose(); // Giải phóng bộ nhớ GPU
            overlayTexture = null;    // Đặt null để tránh dùng texture đã dispose
        }
    }

    /**
     * SceneController bảo phát hiệu ứng nhặt đồ.
     * GameScreen: TODO — sẽ phát sound + particle effect.
     */
    @Override
    public void onPlayPickupEffect(GameObject obj, String itemId) {
        Gdx.app.log("GameScreen", "Pickup effect: " + itemId);
        // TODO: play pickup sound, particle effect
    }

    // =========================================================================
    // RENDER — gọi 60 lần/giây, vẽ mọi thứ lên màn hình
    // =========================================================================

    /**
     * Vẽ 1 frame. Thứ tự vẽ (từ dưới lên):
     * 1. Background + objects (SceneManager)
     * 2. Overlay nếu đang mở (nền mờ + ảnh căn giữa)
     * 3. UI inspect text (Scene2D Stage)
     * 4. Fade đen nếu đang chuyển phòng (ScreenFader)
     *
     * @param delta thời gian từ frame trước (giây)
     */
    @Override
    public void render(float delta) {
        // Xóa màn hình bằng màu đen
        ScreenUtils.clear(0, 0, 0, 1);

        // --- Cập nhật logic (TRƯỚC khi vẽ) ---
        sceneManager.update(delta);      // cập nhật scene (animation object, v.v.)
        sceneController.update(delta);   // cập nhật overlay alpha (fade-in dần)
        screenFader.update(delta);       // cập nhật fade đen (state machine)
        game.getStage().act(delta);      // cập nhật Scene2D UI (animation fade text)

        // --- Vẽ scene (background + objects) ---
        viewport.apply();                              // áp dụng viewport (giữ tỉ lệ 1280x720)
        batch.setProjectionMatrix(camera.combined);    // đồng bộ camera với batch
        batch.begin();                                 // bắt đầu batch (gom draw call)
        sceneManager.render(batch);                    // vẽ background + tất cả object
        batch.end();                                   // kết thúc batch (gửi lên GPU)

        // --- Vẽ overlay (nếu đang mở) ---
        if (sceneController.isOverlayActive()) {
            // Đọc alpha từ SceneController (0→1, tăng dần mỗi frame)
            float overlayAlpha = sceneController.getOverlayAlpha();

            // -- Vẽ nền đen mờ phủ toàn màn hình --
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);         // bật blending để alpha hoạt động
            Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA); // chế độ blend chuẩn
            dimRenderer.setProjectionMatrix(camera.combined);  // đồng bộ camera
            dimRenderer.begin(ShapeRenderer.ShapeType.Filled); // bắt đầu vẽ hình filled
            dimRenderer.setColor(0f, 0f, 0f, 0.65f * overlayAlpha); // đen, 65% mờ × alpha hiện tại
            dimRenderer.rect(0, 0, 1280, 720);         // vẽ hình chữ nhật phủ toàn màn hình
            dimRenderer.end();                          // kết thúc
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);         // tắt blending

            // -- Vẽ ảnh overlay căn giữa màn hình --
            if (overlayTexture != null) {
                // Tính toán scale để ảnh vừa khung 700×550 (giữ tỉ lệ gốc)
                float maxW = 700f;                                // chiều rộng tối đa
                float maxH = 550f;                                // chiều cao tối đa
                float imgW = overlayTexture.getWidth();           // chiều rộng ���nh gốc (pixel)
                float imgH = overlayTexture.getHeight();          // chiều cao ảnh gốc (pixel)
                float scale = Math.min(maxW / imgW, maxH / imgH); // scale nhỏ hơn để vừa khung
                float drawW = imgW * scale;                       // chiều rộng sau scale
                float drawH = imgH * scale;                       // chiều cao sau scale
                float drawX = (1280 - drawW) / 2f;               // X căn giữa màn hình
                float drawY = (720 - drawH) / 2f;                // Y căn giữa màn hình

                batch.begin();
                batch.setColor(1f, 1f, 1f, overlayAlpha);        // set alpha cho ảnh (fade-in)
                batch.draw(overlayTexture, drawX, drawY, drawW, drawH); // vẽ ảnh
                batch.setColor(Color.WHITE);                      // reset color về mặc định
                batch.end();
            }
        }

        // --- Vẽ UI (inspect text) ---
        // Stage.draw() vẽ tất cả Actor đã thêm (inspectTable, v.v.)
        game.getStage().draw();

        // --- Vẽ fade đen (trên cùng) ---
        // Nếu đang chuyển phòng → ScreenFader vẽ hình chữ nhật đen phủ lên tất cả
        screenFader.render();
    }

    // =========================================================================
    // RESIZE / HIDE / DISPOSE — lifecycle của Screen
    // =========================================================================

    /**
     * Gọi khi thay đổi kích thước cửa sổ.
     * Cập nhật viewport để giữ tỉ lệ 1280×720.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);                                    // cập nhật viewport game
        game.getStage().getViewport().update(width, height, true);      // cập nhật viewport UI
    }

    /**
     * Gọi khi rời GameScreen (chuyển sang screen khác).
     * Dọn dẹp input và UI.
     */
    @Override
    public void hide() {
        inputHandler.clear();                            // xóa input processor
        if (inspectTable != null) inspectTable.remove();  // xóa inspectTable khỏi Stage
        onCloseOverlay();                                 // dispose overlay texture nếu đang mở
    }

    /**
     * Gọi khi GameScreen bị hủy hoàn toàn.
     * Giải phóng TẤT CẢ tài nguyên (bộ nhớ GPU, font, v.v.).
     */
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();               // giải phóng SpriteBatch
        if (dimRenderer != null) dimRenderer.dispose();   // giải phóng ShapeRenderer
        if (vietnameseFont != null) vietnameseFont.dispose(); // giải phóng BitmapFont
        if (fontGenerator != null) fontGenerator.dispose();   // giải phóng FreeType generator
        if (overlayTexture != null) overlayTexture.dispose(); // giải phóng overlay texture
    }
}
