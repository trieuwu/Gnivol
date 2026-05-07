package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.gnivol.game.GnivolGame;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class LoginScreen extends BaseScreen {

    private Stage stage;
    private Skin skin;
    private BitmapFont vietnameseFont;
    private FreeTypeFontGenerator fontGenerator;
    private static final String VIETNAMESE_CHARS =
        "aăâbcdđeêfghijklmnoôơpqrstuưvwxyz"
            + "AĂÂBCDĐEÊFGHIJKLMNOÔƠPQRSTUƯVWXYZ"
            + "àáảãạằắẳẵặầấẩẫậèéẻẽẹềếểễệìíỉĩịòóỏõọồốổỗộờớởỡợùúủũụừứửữựỳýỷỹỵ"
            + "ÀÁẢÃẠẰẮẲẴẶẦẤẨẪẬÈÉẺẼẸỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌỒỐỔỖỘỜỚỞỠỢÙÚỦŨỤỪỨỬỮỰỲÝỶỸỴ"
            + "0123456789.,;:!?'\"-()[]{}…—–/\\@#$%^&*+=<>~`| ";

    // UI Elements
    private Label terminalText;
    private Label loadingLabel;
    private TextField nameInput;
    private TextButton startButton;
    private Label errorLabel;

    // Typewriter effect variables
    private String fullText = "Gnivol v1.3.6 System\nCreating...";
    private String currentText = "";
    private float typeTimer = 0f;
    private int charIndex = 0;
    private final float TYPE_SPEED = 0.05f; // Tốc độ gõ
    private boolean isTypingDone = false;

    // Loading variables
    private float loadTimer = 0f;
    private boolean isLoadingDone = false;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    // Màu xanh Terminal chuẩn
    private final Color TERMINAL_GREEN = Color.valueOf("#00FF41");

    public LoginScreen(GnivolGame game) {
        super(game);
    }

    @Override
    public void show() {
        game.getStage().clear();
        stage = game.getStage();
        Gdx.input.setInputProcessor(stage);
        stage.clear();

        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        // Tạm thời dùng skin mặc định, bạn có thể tự thay style sau
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PressStart2P-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 22; // Cỡ chữ
        param.spaceY = 15;
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        vietnameseFont = fontGenerator.generateFont(param);

        // 1. Label hiển thị chữ gõ (Typewriter)
        Label.LabelStyle labelStyle = new Label.LabelStyle(vietnameseFont, TERMINAL_GREEN);
        terminalText = new Label("", labelStyle);
        terminalText.setPosition(100, 600);
        stage.addActor(terminalText);

        // 2. Loading Label
        loadingLabel = new Label("", labelStyle);
        loadingLabel.setPosition(100, 500);
        stage.addActor(loadingLabel);

        // 3. Input Nhập Tên (Ẩn lúc đầu)
        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        fieldStyle.font = vietnameseFont;
        fieldStyle.fontColor = TERMINAL_GREEN;

        nameInput = new TextField("", fieldStyle);
        nameInput.setMessageText("Your name...");
        nameInput.setMaxLength(30);
        nameInput.setPosition(100, 400);
        nameInput.setSize(350, 45);
        nameInput.setVisible(false);
        nameInput.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                    c == ' ';
            }
        });

        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("GnivolSettings");
        String savedName = prefs.getString("playerName", "");
        if (!savedName.isEmpty()) {
            nameInput.setText(savedName);
            nameInput.setCursorPosition(savedName.length());
        }

        stage.addActor(nameInput);

        // 4. Label Báo Lỗi (Validate rỗng)
        errorLabel = new Label("Enter your name!", new Label.LabelStyle(vietnameseFont, Color.RED));
        errorLabel.setPosition(100, 360);
        errorLabel.setVisible(false);
        stage.addActor(errorLabel);

        // 5. Nút Bắt Đầu
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        btnStyle.font = vietnameseFont;
        btnStyle.fontColor = TERMINAL_GREEN;

        startButton = new TextButton("START", btnStyle);
        startButton.setPosition(100, 280);
        startButton.setSize(150, 45);
        startButton.setVisible(false);

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String playerName = nameInput.getText().trim();

                if (playerName.isEmpty()) {
                    errorLabel.setVisible(true);
                } else {
                    com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("GnivolSettings");
                    prefs.putString("playerName", playerName);
                    prefs.flush();
                    errorLabel.setVisible(false);
                    game.getGameState().setPlayerName(playerName);
                    Gdx.app.log("Login", "Welcome baby: " + playerName);
                    final boolean suicided = game.getEndingManager() != null
                        && game.getEndingManager().isSuicided();
                    if (game.getScreenFader() != null && !game.getScreenFader().isFading()) {
                        game.getScreenFader().startFade(() -> {
                            Gdx.app.postRunnable(() -> {
                                if (suicided) {
                                    game.setScreen(new SuicideIntroScreen(game));
                                } else {
                                    game.setScreen(new MainMenuScreen(game));
                                }
                                LoginScreen.this.dispose();
                            });
                        });
                    } else {

                        if (suicided) {
                            game.setScreen(new SuicideIntroScreen(game));
                        } else {
                            game.setScreen(new MainMenuScreen(game));
                        }
                    }

                }
            }
        });
        stage.addActor(startButton);
    }

    @Override
    public void render(float delta) {
        // Nền đen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float flickerAlpha = 0.95f + (float) Math.random() * 0.05f;

        if (Math.random() < 0.02f) {
            flickerAlpha = 0.6f;
        }

        terminalText.getColor().a = flickerAlpha;
        loadingLabel.getColor().a = flickerAlpha;
        errorLabel.getColor().a = flickerAlpha;

        // --- Logic Typewriter
        if (!isTypingDone) {
            typeTimer += delta;
            if (typeTimer >= TYPE_SPEED && charIndex < fullText.length()) {
                currentText += fullText.charAt(charIndex);
                terminalText.setText(currentText);
                charIndex++;
                typeTimer = 0f;
                if (charIndex == fullText.length()) isTypingDone = true;
            }
        }
        // --- Logic Loading
        else if (!isLoadingDone) {
            loadTimer += delta;
            if (loadTimer < 1f) loadingLabel.setText("Connecting [##........]");
            else if (loadTimer < 2f) loadingLabel.setText("Connecting [######....]");
            else if (loadTimer < 3f) loadingLabel.setText("Connecting [##########]");
            else {
                isLoadingDone = true;
                loadingLabel.setText("Success Connected! What is your name?");
                nameInput.setVisible(true);
                startButton.setVisible(true);
                stage.setKeyboardFocus(nameInput);
            }
        }

        stage.act(delta);
        stage.draw();

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.35f);

        for (int y = 0; y < Gdx.graphics.getHeight(); y += 4) {
            shapeRenderer.rect(0, y, Gdx.graphics.getWidth(), 2);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        if (game.getScreenFader() != null) {
            game.getScreenFader().update(delta);
            game.getScreenFader().render(); 
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (vietnameseFont != null) vietnameseFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
        super.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        if (stage != null) {
            stage.clear();
        }
    }
}
