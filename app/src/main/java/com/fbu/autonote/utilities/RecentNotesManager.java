package com.fbu.autonote.utilities;

import android.content.Context;
import android.util.Log;

import com.fbu.autonote.models.Note;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class RecentNotesManager {
    File file;
    Context context;
    public static final String TAG  = "RecentNotesManager";
    public static final String FILENAME = "recentNotes.txt";
    public static final int CAPACITY = 5;
    LRUCache<String> cache;
    private static final RecentNotesManager instance = new RecentNotesManager();
    /**
     * Implementation of the generic LRU Cache made specifically for recent note managing
     * @class Singleton that manages recent notes
     */
    public RecentNotesManager() { }

    //This should only be done once
    public void initialize(Context context) {
        instance.context = context;
        cache = new LRUCache<>(CAPACITY);
        try {
            loadFile();
        } catch (IOException e) {
            Log.e(TAG, "Could not load savefile: " + e.toString());
        }
    }

    public static RecentNotesManager getInstance() {
        return instance;
    }

    private void loadFile() throws IOException {
        file = new File(context.getFilesDir(), FILENAME);
        if (file.exists()) {
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String noteUri = reader.readLine();
            while(noteUri != null) {
                cache.update(noteUri);
                noteUri = reader.readLine();
            }
        } else {
            file.createNewFile();
        }
    }

    public void registerUse(Note note) {
        cache.update(note.getUrl());
        save();
    }

    public LRUCache<String> getContainer() {
        return cache;
    }

    public void save() {
            try (FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
                for (LRUCache<String> it = cache; it.hasNext(); ) {
                    String noteUri = it.next();
                    Log.d(TAG, "Saving note with uri: " + noteUri);
                    fos.write(noteUri.getBytes());
                    fos.write("\n".getBytes());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

