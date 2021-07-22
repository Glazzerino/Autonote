package com.fbu.autonote.utilities;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

public class Favorites {
    /**
     * @class manages the CRUD-like favorites list
     */
    File favsFile;
    Set<String> favoritesSet;
    Context context;
    OutputStreamWriter writer;

    private static final String FILENAME = "Favorites.txt";
    public static final String TAG = "FavoriteSystem";

    public Favorites(Context context) {
        favoritesSet = new HashSet<>(20, 0.8f);
        this.context = context;
        try {
            loadData();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void attemptInsert(String uri) {
        if (!favoritesSet.add(uri)) {
            Log.e(TAG, String.format("Error; element already in list: [%s]", uri));
        }
    }

    public void addFav(String newNoteUri) {
        attemptInsert(newNoteUri);
        save();
    }

    private void save() {
        try (FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            for (String uri : favoritesSet) {
                byte[] newline = new String("\n\r").getBytes();
                fos.write(uri.getBytes());
                fos.write(newline);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getFavoritesList() {
        return favoritesSet;
    }

    public void remove(String uri) {
        favoritesSet.remove(uri);
        save();
    }

    public boolean checkIfFavorite(String uri) {
        return favoritesSet.contains(uri);
    }

    private void loadData() throws IOException {
        favsFile = new File(context.getFilesDir(), FILENAME);
        if (favsFile.exists()){
            FileInputStream inputStream = new FileInputStream(favsFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String fileLine = reader.readLine();

            while(fileLine != null) {
                if (!favoritesSet.contains(fileLine)) {
                    attemptInsert(fileLine);
                    Log.d(TAG, "Fileline: " + fileLine);
                } else {
                    Log.e(TAG, "Error: item already in set");
                }
                fileLine = reader.readLine();
            }
        }
    }
}
