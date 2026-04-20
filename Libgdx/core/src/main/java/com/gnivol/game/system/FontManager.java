package com.gnivol.game.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {
    public static final String VIETNAMESE_CHARS =
        "aăâbcdđeêfghijklmnoôơpqrstuưvwxyz"
            + "AĂÂBCDĐEÊFGHIJKLMNOÔƠPQRSTUƯVWXYZ"
            + "àáảãạằắẳẵặầấẩẫậèéẻẽẹềếểễệìíỉĩịòóỏõọồốổỗộờớởỡợùúủũụừứửữựỳýỷỹỵ"
            + "ÀÁẢÃẠẰẮẲẴẶẦẤẨẪẬÈÉẺẼẸỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌỒỐỔỖỘỜỚỞỠỢÙÚỦŨỤỪỨỬỮỰỲÝỶỸỴ"
            + "0123456789.,;:!?'\"-()[]{}…—–/\\@#$%^&*+=<>~`| ";


    public BitmapFont fontTitle;
    public BitmapFont fontButton;
    public BitmapFont fontVietnamese;
    public BitmapFont fontDebug;
    public FontManager() {
        generateFonts();
    }

    private void generateFonts() {
        FreeTypeFontGenerator mainGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellEnglish.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParam.size = 52;
        titleParam.borderWidth = 2f;
        titleParam.borderColor = Color.DARK_GRAY;
        fontTitle = mainGen.generateFont(titleParam);

        FreeTypeFontGenerator.FreeTypeFontParameter btnParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        btnParam.size = 28;
        btnParam.borderWidth = 1f;
        btnParam.borderColor = Color.BLACK;
        fontButton = mainGen.generateFont(btnParam);

        mainGen.dispose();

        FreeTypeFontGenerator viGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter viParam = new FreeTypeFontGenerator.FreeTypeFontParameter();

        viParam.size = 26;
        viParam.borderWidth = 2f;
        viParam.borderColor = Color.BLACK;
        viParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        fontVietnamese = viGen.generateFont(viParam);


        viParam.size = 14;
        viParam.borderWidth = 0f;
        fontDebug = viGen.generateFont(viParam);

        viGen.dispose();
    }

    public void dispose() {
        if (fontTitle != null) fontTitle.dispose();
        if (fontButton != null) fontButton.dispose();
        if (fontVietnamese != null) fontVietnamese.dispose();
        if (fontDebug != null) fontDebug.dispose();
    }
}
