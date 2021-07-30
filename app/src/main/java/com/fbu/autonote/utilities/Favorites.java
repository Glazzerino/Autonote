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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Favorites {
    /**
     * @class manages the CRUD-like favorites list
     */
    File favsFile;
    Context context;
    public static final float LOAD_FACTOR = 0.8f;
    HashMap<String, Set<String>> mainContainer;
    private static final String FILENAME = "favorites.txt";
    public static final String TAG = "Favorites";
    public static Favorites instance = new Favorites();

    public Favorites() { }

    public static Favorites getInstance() {
        return instance;
    }

    public void initialize(Context context) {
        instance.mainContainer = new HashMap<>(20, LOAD_FACTOR);
        instance.context = context;
        try {
            loadData();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public List<String> getAll() {
        List<String> allUris = new ArrayList<>(20);
        for (Set<String> uris : mainContainer.values()) {
            allUris.addAll(uris);
        }
        return allUris;
    }

    private void attemptInsert(String topic, String uri) {
        if (!mainContainer.containsKey(topic)) {
            mainContainer.put(topic, new HashSet<>(20, LOAD_FACTOR));
        }
        mainContainer.get(topic).add(uri);
        Log.d(TAG, String.format("Item added to %s: %s", topic, uri));
    }

    public void addFav(String newNoteUri, String topic) {
        attemptInsert(topic, newNoteUri);
        save();
    }

    private void save() {
        try (FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            //Save each entry inside the
            for (String topic : mainContainer.keySet()) {
                Log.d(TAG, topic);
                for (String uri : mainContainer.get(topic)) {
                    String writeLine = topic + "_" + uri;
                    byte[] newline = "\n".getBytes();
                    fos.write(writeLine.getBytes());
                    fos.write(newline);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getFavoritesList(String topic) throws NullPointerException{
        return mainContainer.get(topic);
    }

    public boolean remove(String uri, String topic) {
        try {
            mainContainer.get(topic).remove(uri);
            save();
            return true;
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public boolean checkIfFavorite(String uri, String topic) {
        if (mainContainer.containsKey(topic)) {
            boolean result = mainContainer.get(topic).contains(uri);
            Log.d(TAG, "CHECKED IF FAVORITE: " + result);
            return result;
        }
        return false;
    }

    private void loadData() throws IOException {
        favsFile = new File(context.getFilesDir(), FILENAME);
        if (favsFile.exists()){
            FileInputStream inputStream = new FileInputStream(favsFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String fileLine = reader.readLine();

            while(fileLine != null) {
                //Note topic goes before the note uri, behind the "_" character
                Log.d(TAG, "file line: " + fileLine);
                int separatorIndex = fileLine.indexOf("_");
                String topic = null;
                String uri = null;
                try {
                     topic = fileLine.substring(0, separatorIndex);
                     uri = fileLine.substring(separatorIndex + 1);
                    //If hashmap has not initialized the topic set then do so
                    attemptInsert(topic, uri);
                } catch (StringIndexOutOfBoundsException e) {
                    Log.e(TAG, e.toString());
                }
                fileLine = reader.readLine();
            }
            displayContainer();
        }
    }

    private void displayContainer() {
        for (String topic : mainContainer.keySet()) {
            Log.d(TAG, "__"+topic+"__");
            for (String uri : mainContainer.get(topic)) {
                Log.d(TAG, uri);
            }
        }
    }


}
