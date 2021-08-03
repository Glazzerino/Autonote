package com.fbu.autonote.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.fbu.autonote.R;
import com.fbu.autonote.fragments.ProfileFragment;
import com.fbu.autonote.fragments.ScanResultsFragment;
import com.fbu.autonote.fragments.TopicSelectionFragment;
import com.fbu.autonote.models.Note;
import com.fbu.autonote.utilities.Favorites;
import com.fbu.autonote.utilities.RecentNotesManager;
import com.fbu.autonote.utilities.uClassifyRequestMode;
import com.geniusscansdk.core.GeniusScanSDK;
import com.geniusscansdk.core.LicenseException;
import com.geniusscansdk.scanflow.ScanConfiguration;
import com.geniusscansdk.scanflow.ScanFlow;
import com.geniusscansdk.scanflow.ScanResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import me.ibrahimsn.lib.OnItemReselectedListener;
import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    Fragment fragment;
    FragmentManager fragmentManager;
    Context context;
    List<ScanResult.Scan> scans;
    FirebaseFunctions firebaseFunctions;
    FirebaseAuth authManager;
    DatabaseReference databaseReference;
    String userId;
    StorageReference imageStorage;
    OkHttpClient client;
    SmoothBottomBar bottomBar;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        context = this;
        bottomBar = findViewById(R.id.bottomBar);
        authManager = FirebaseAuth.getInstance();
        userId = authManager.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference(userId);
        imageStorage = FirebaseStorage.getInstance().getReference(userId);
        firebaseFunctions = FirebaseFunctions.getInstance();
        client = new OkHttpClient();
        RecentNotesManager.getInstance().initialize(this);

        //Init GeniusSDK
        try {
            GeniusScanSDK.init(context, getString(R.string.genius_apikey));
            Log.d("MainActivity", "GeniusSDK initialized");
        } catch (LicenseException exception) {
            Log.e("MainActivity", exception.toString());
        }

        //Initialize Favorites singleton
        Favorites.getInstance().initialize(this);

        //Set bottom menu button actions
        //TODO: PROFILE VIEW
        //TODO: NOTES VIEW

        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                switch (i) {
                    case 0:
                        fragment = TopicSelectionFragment.newInstance(context);
                        break;
                    case 1:
                        initScanner();
                        break;
                    case 2:
                        fragment = ProfileFragment.newInstance();
                        break;
                    default:
                        Log.e(TAG, "Error; menu item index out of bounds");
                        break;
                }
                startFragment();
                return true;
            }
        });

        fragment = TopicSelectionFragment.newInstance(this);
        startFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            ScanResult result = ScanFlow.getScanResultFromActivityResult(data);
            scans = result.scans;
            fragment = ScanResultsFragment.newInstance(scans);
            startFragment();

            String newNoteCollectionId = randomId();
            List<Note> newNotes = new ArrayList<>();
            Task<List<Task<Uri>>> uploadTasks = getUploadImageTasks(newNoteCollectionId, newNotes);
            Task<List<Task<JsonElement>>> annotationTasks = getAnnotationTasks();

            //Main chaining of tasks
            uploadTasks.continueWithTask(new Continuation<List<Task<Uri>>, Task<List<Task<JsonElement>>>>() {
                @Override
                public Task<List<Task<JsonElement>>> then(@NonNull @NotNull Task<List<Task<Uri>>> tasks) throws Exception {
                    Toasty.info(context, "Files uploaded to the cloud!", Toast.LENGTH_LONG).show();
                    return annotationTasks;
                }
            }).continueWith(new Continuation<List<Task<JsonElement>>, Void>() {
                //After uploading the images we invoke the cloud function to get text from the images
                @Override
                public Void then(@NonNull @NotNull Task<List<Task<JsonElement>>> annotTask) {
                    getTextsFromAnnotationTask(annotTask, newNotes);
                    Log.d(TAG, "New notes size: " + newNotes.size());
                    // Use detected texts and feed them to topic detection API
                    Request request = getUclassifyRequest(newNotes, uClassifyRequestMode.CLASSIFY);
                    //TODO: find a way to wrap okHttp call in a Task implementation for better chaining
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.e(TAG, e.toString());
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String data = response.body().string();
                            Log.d(TAG, data);
                            processTopicApiResponse(data, newNotes);
                            for (Note notes : newNotes) {
                                Log.d(TAG, "Topic: " + notes.getTopic());
                            }
                            //task chain continues from this function
                            executeKeywordsApiCall(newNotes, newNoteCollectionId);
                        }
                    });
                    return null;
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", e.toString());
        }
    }

    private void doRealtimeDbUpload(String randomId, List<Note> newNotes) {
        String collectionId = randomId();
        Task<List<Task<Void>>> uploadDataToDbTask = getUploadToDatabase(randomId, newNotes);
        uploadDataToDbTask.addOnSuccessListener(new OnSuccessListener<List<Task<Void>>>() {
            @Override
            public void onSuccess(List<Task<Void>> tasks) {
                fragment.getView().findViewById(R.id.progressIndicator)
                        .setVisibility(View.INVISIBLE);
                Toasty.success(context, "Notes processed!", Toasty.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @return a random 64-bit number string
     */
    @NotNull
    private String randomId() {
        return String.valueOf(Math.abs(new Random().nextLong()));
    }

    private void initScanner() {
        ScanConfiguration scanConfiguration = new ScanConfiguration();
        scanConfiguration.multiPage = true;
        ScanFlow.scanWithConfiguration(this, scanConfiguration);
    }

    private void startFragment() {
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.flFragmentContainer, fragment)
                    .commit();
        }
    }

    private Task<List<Task<Uri>>> getUploadImageTasks(String noteCollectionId, List<Note> newNotes) {
        Toasty.info(context, "Uploading scans to the cloud", Toast.LENGTH_SHORT).show();
        StorageReference newNoteStorage = imageStorage
                .child(noteCollectionId);
        List<Task<Uri>> tasks = new ArrayList<>();

        //Using a liked list since byte arrays should need more space and thus more memory allocation
        List<byte[]> byteArrays = new LinkedList<>();
        //upload each file to the newNote directory
        for (ScanResult.Scan scan : scans) {
            File imageFile = scan.enhancedImageFile;
            //Reference to new file in Firebase. Give it the name of the on-device file
            StorageReference imageReference = newNoteStorage.child(imageFile.getName());
            //Upload file
            Uri fileUri = Uri.fromFile(imageFile);
            UploadTask upload = imageReference.putFile(fileUri);
            //Set event listeners for each task
            upload.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Log.e(TAG,
                            String.format("Error uploading file %s : %s",
                                    imageFile.getName(),
                                    e.toString()));
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Success uploading file " + imageFile.getName());
                    Note note = new Note();
                    note.setNoteId(randomId());

                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            note.setImageURL(uri.toString());
                        }
                    });
                    newNotes.add(note);
                }
            });
            tasks.add((Task) upload);
        }

        //Use all stored tasks in the task list
        Task<List<Task<Uri>>> allUploads = Tasks.whenAllSuccess(tasks);
        return allUploads;
    }

    /**
     * @param collectionId name of the "directory" in firebase for the new collection of notes
     * @param newNotes     list of notes from which to pull data from
     * @return Meta-task of database uploading tasks
     */
    private Task<List<Task<Void>>> getUploadToDatabase(String collectionId, List<Note> newNotes) {

        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
        String nowDate = ISO_8601_FORMAT.format(new Date());
        DatabaseReference noteReference;
        List<Task<Void>> uploadTasksList = new ArrayList<>();
        //Iterate over each Note object to upload its data to the realtime database
        for (int i = 0; i < newNotes.size(); i++) {
            Note note = newNotes.get(i);
            note.setDate(nowDate);
            //add note as topic/collecitonId/noteId inside database filesystem
            noteReference = databaseReference
                    .child(note.getTopic())
                    .child(nowDate)
                    .child(note.getNoteId());
            String cleanText = note.getTextContent().replace("\\n", " ");
            note.setTextContent(cleanText);
            DatabaseReference keywords = noteReference.child("keywords");
            for (String keyword : note.getKeywords()) {
                noteReference.setValue(note.getKeywords());
            }
            uploadTasksList.add(noteReference.setValue(note));
        }
        Task<List<Task<Void>>> uploadTasks = Tasks.whenAllSuccess(uploadTasksList);
        return uploadTasks;
    }

    private Task<List<Task<JsonElement>>> getAnnotationTasks() {

        List<Task<JsonElement>> annotationTasksList = new ArrayList<>();
        //Iterate over each scanned image and register an upload task
        for (ScanResult.Scan scan : scans) {
            byte[] byteArrayImage = getByteArray(scan.enhancedImageFile);
            JsonObject request = getAnnotationRequest(byteArrayImage);

            Task<JsonElement> annotationTask = getAnnotateImageTask(request.toString());
            //Add a failure listener to know which task failed and why
            annotationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Log.e(TAG, "Error getting transcription: " + e.toString());
                }
            });
            annotationTasksList.add(annotationTask);
        }
        //this object accumulates all tasks' results and listens to them as a single result
        Task<List<Task<JsonElement>>> allAnnotationTasks = Tasks.whenAllSuccess(annotationTasksList);
        return allAnnotationTasks;
    }

    @NotNull
    private JsonObject getAnnotationRequest(byte[] byteArrayImage) {
        String encodedB64 = Base64.encodeToString(byteArrayImage, Base64.NO_WRAP);
        JsonObject request = new JsonObject();

        JsonObject image = new JsonObject();
        image.add("content", new JsonPrimitive(encodedB64));
        request.add("image", image);

        JsonObject feature = new JsonObject();
        feature.add("type", new JsonPrimitive("DOCUMENT_TEXT_DETECTION"));
        JsonArray features = new JsonArray();
        features.add(feature);
        request.add("features", features);

        JsonObject imageContext = new JsonObject();
        JsonArray languageHints = new JsonArray();
        languageHints.add("en");
        languageHints.add("es");
        imageContext.add("languageHints", languageHints);
        request.add("imageContext", imageContext);
        return request;
    }

    private Task<JsonElement> getAnnotateImageTask(String request) {
        return firebaseFunctions
                .getHttpsCallable("annotateImage")
                .call(request)
                .continueWith(new Continuation<HttpsCallableResult, JsonElement>() {
                    @Override
                    public JsonElement then(@NonNull Task<HttpsCallableResult> task) {
                        return JsonParser.parseString(new Gson().toJson(task.getResult().getData()));
                    }
                });
    }

    /**
     * @param newNotes list of notes which contain the raw text which will be classified
     * @param reqMode  enum object that determines the API call mode. More info here: <a href="https://uclassify.com/docs/restapi#readcalls-classify"/>
     * @return request object
     * @about Function fabricates a request object meant to get topic classification data
     */
    public Request getUclassifyRequest(List<Note> newNotes, uClassifyRequestMode reqMode) {
        JSONArray texts = new JSONArray();
        for (Note note : newNotes) {
            String rawText = note.getTextContent();
            texts.put(rawText);
        }
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("texts", texts);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getString(R.string.uclassify_taxonomy_url) +
                (reqMode == uClassifyRequestMode.CLASSIFY ? "classify" : "keywords");

        RequestBody body = RequestBody.create(JSON, bodyJson.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Token " + getString(R.string.uclassify_readkey))
                .build();
        return request;
    }

    private void processTopicApiResponse(String data, List<Note> newNotes) {
        //Get json objects from the response gotten from uClassify
        //More info about response format here: https://uclassify.com/browse/uclassify/iab-taxonomy?input=Text
        JSONArray textsResults = null;
        try {
            textsResults = new JSONArray(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < textsResults.length(); j++) {
            JSONArray classifications = null;
            try {
                classifications = textsResults.getJSONObject(j).getJSONArray("classification");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Iterate over each topic and extract the one with the most weight
            double max = 0;
            String maxTopic = "";
            for (int i = 0; i < classifications.length(); i++) {
                JSONObject topicJson = null;
                try {
                    topicJson = classifications.getJSONObject(i);
                    double weight = topicJson.getDouble("p");
                    if (weight > max) {
                        max = weight;
                        maxTopic = topicJson.getString("className");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Trim the rest of the topic string up to the underscore
            maxTopic = maxTopic.substring(0, maxTopic.indexOf("_"));
            newNotes.get(j).setTopic(maxTopic);
        }
    }

    //Utility function
    @NotNull
    private byte[] getByteArray(File imageFile) {
        Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return byteArray;
    }

    /**
     * @param tasks    Task container of the annotation tasks return by the getAnnotationTasks method
     * @param newNotes reference to list of note objects
     */
    private void getTextsFromAnnotationTask(Task<List<Task<JsonElement>>> tasks, List<Note> newNotes) {
        int i = 0;
        for (Object rawObject : tasks.getResult()) {
            JsonArray json = (JsonArray) rawObject;
            JsonObject annotation = json.get(0)
                    .getAsJsonObject()
                    .get("fullTextAnnotation")
                    .getAsJsonObject();
            String text = annotation.get("text").toString();
            Log.d(TAG, String.format("%s%n", text));
            System.out.format("%s%n", annotation.get("text").getAsString());
            newNotes.get(i++).setTextContent(text);
        }
    }

    private void executeKeywordsApiCall(List<Note> newNotes, String newNoteCollectionId) {
        Request keywordsRequest = getUclassifyRequest(newNotes, uClassifyRequestMode.KEYWORDS);
        client.newCall(keywordsRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    processKeywordsApiResponse(response.body().string(),
                            newNotes,
                            newNoteCollectionId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @param data raw response string from the keywords API call
     * @throws JSONException
     */
    private void processKeywordsApiResponse(String data, List<Note> newNotes, String newNoteCollectionId) throws JSONException {
        Log.d(TAG, "keyword data: " + data);
        JSONArray responseArray = new JSONArray(data);
        //The keywords API call returns a JSON array for each text provided to it.
        //Each of these arrays contains yet another array, which has 560 elements, a class name (topic),
        // a keyword and a weight (confidence in topic)
        for (int j = 0; j < newNotes.size(); j++) {
            JSONArray individualTextArray = responseArray.getJSONArray(j);
            individualTextArray = responseArray.getJSONArray(j);
            String noteTopic = newNotes.get(j).getTopic();

            //get general topic from topic result
            noteTopic = noteTopic.substring(0, noteTopic.indexOf("_") + 1);
            Log.d(TAG, "Keyword topic: " + noteTopic);
            for (int i = 0; i < individualTextArray.length(); i++) {
                Note note = newNotes.get(j);
                JSONObject responseObj = individualTextArray.getJSONObject(i);
                String wordCandidate = responseObj.getString("keyword");
                //if word is related to note topic and word is only made of alphabetic characters
                if (responseObj.getString("className").contains(noteTopic) && isAlphabeticString(wordCandidate)) {
                    String keyword = responseObj.getString("keyword");
                    note.addKeyword(keyword);
                    Log.d(TAG, "Keyword: " + keyword);
                }
            }
        }
        doRealtimeDbUpload(newNoteCollectionId, newNotes);
    }

    //utility function to tell if a string is composed of only alphabetic characters
    private boolean isAlphabeticString(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }
}
