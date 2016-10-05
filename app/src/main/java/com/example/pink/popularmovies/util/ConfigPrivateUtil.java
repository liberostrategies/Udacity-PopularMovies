package com.example.pink.popularmovies.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by pink on 10/5/2016.
 */

public class ConfigPrivateUtil {
    /** My themoviedb service's API key. */
    public static final String THEMOVIEDB_API = "themoviedb.apikey";

    /**
     * Get the property value for the specified key from config-private.properties.
     * @param key Key in properties file.
     * @param context  Retrieves application resource files.
     * @return Value of key
     * @throws IOException
     */
    public static String getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("config-private.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }

}
