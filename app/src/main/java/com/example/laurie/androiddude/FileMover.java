package com.example.laurie.androiddude;
        import android.content.res.AssetManager;
        import android.util.Log;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;

/**
 * Created by james adams on 02/11/16.
 */

public class FileMover {
    public void copyFile(MainActivity m, String filename) {
        AssetManager assetManager = m.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = "/sdcard/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[2096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

}
