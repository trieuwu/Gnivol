package com.gnivol.game.system.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class OverlayManager {
    private Texture texture;
    private boolean active;
    private float alpha;
    private String sourceId;
    private final ShapeRenderer dimRenderer;

    public OverlayManager() {
        this.dimRenderer = new ShapeRenderer();
    }

    public void open(String texturePath, String sourceId) {
        try {
            if (texture != null) texture.dispose();
            texture = new Texture(Gdx.files.internal(texturePath));
            active = true;
            alpha = 0f;
            this.sourceId = sourceId;
        } catch (Exception e) {
            Gdx.app.error("OverlayManager", "Cannot load: " + texturePath, e);
        }
    }

    public void close() {
        active = false;
        sourceId = null;
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    public boolean isActive() { return active; }
    public String getSourceId() { return sourceId; }
    public Texture getTexture() { return texture; }

    public void swapTexture(String texturePath) {
        try {
            if (texture != null) texture.dispose();
            texture = new Texture(Gdx.files.internal(texturePath));
        } catch (Exception e) {
            Gdx.app.error("OverlayManager", "Cannot swap: " + texturePath, e);
        }
    }

    public void render(float delta, SpriteBatch batch, com.badlogic.gdx.graphics.OrthographicCamera camera) {
        if (!active) return;
        alpha = Math.min(alpha + delta * 4f, 1f);

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        dimRenderer.setProjectionMatrix(camera.combined);
        dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dimRenderer.setColor(0, 0, 0, 0.65f * alpha);
        dimRenderer.rect(0, 0, 1280, 720);
        dimRenderer.end();


        if (texture != null) {
            batch.begin();
            batch.setColor(1, 1, 1, alpha);
            batch.draw(texture, (1280 - 700) / 2f, (720 - 550) / 2f, 700, 550);
            batch.setColor(Color.WHITE);
            batch.end();
        }
    }

    public Vector2 getRelativeClick(float worldX, float worldY) {
        if (texture == null) return null;
        float maxW = 700f, maxH = 550f;
        float imgW = texture.getWidth(), imgH = texture.getHeight();
        float scale = Math.min(maxW / imgW, maxH / imgH);
        float drawX = (1280 - (imgW * scale)) / 2f;
        float drawY = (720 - (imgH * scale)) / 2f;

        float relX = (worldX - drawX) / scale;
        float relY = (worldY - drawY) / scale;
        return new Vector2(relX, relY);
    }

    public void dispose() {
        if (dimRenderer != null) dimRenderer.dispose();
        if (texture != null) texture.dispose();
    }
}
