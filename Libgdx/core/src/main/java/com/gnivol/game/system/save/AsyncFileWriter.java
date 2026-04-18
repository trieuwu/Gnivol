package com.gnivol.game.system.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncFileWriter {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface SaveCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void writeAsync(final String json, final String path, final SaveCallback callback) {
        executor.submit(() -> {
            try {
                // Tạo thư mục/file ẩn ~/.gnivol ở thư mục gốc của máy tính
                FileHandle file = Gdx.files.external(".gnivol/" + path);
                file.writeString(json, false, "UTF-8");

                // Trả callback về luồng chính (Main Thread) của LibGDX để update UI
                Gdx.app.postRunnable(callback::onSuccess);
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        });
    }

    public void dispose() {
        executor.shutdown();
    }
}
