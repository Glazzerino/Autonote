package com.fbu.autonote.utilities;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TSEngine {
    File file;
    Context context;
    public static final String TAG = "TSEngine";
    JsonObject invertedIndexSource;
    //Very common english words that can be excluded from indexing
    private static final HashSet<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "and", "be", "have", "i", "in", "of", "that", "the", "to"
    ));

    public TSEngine(Context context) {
        this.context = context;
    }

    private void loadFile() throws IOException {
        file = new File(context.getFilesDir(), "invertedIndex.txt");
        if (file.exists()) {
            //Iterate over each string using Java 8 Stream class
            try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
                stream.forEach(new Consumer<String>() {
                    @Override
                    // format: [token]SPACE[file]SPACE[file]...
                    public void accept(String s) {
                        String[] lineTokens = s.split(" ");
                        if (lineTokens.length != 0) {
                            String word = lineTokens[0];
                            List<String> uris = new LinkedList<>();
                            for (int i = 1; i< lineTokens.length; i++) {
                                uris.add(lineTokens[i]);
                            }
                        }
                    }
                });
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Could not create file!");
            }
        }
    }
}
