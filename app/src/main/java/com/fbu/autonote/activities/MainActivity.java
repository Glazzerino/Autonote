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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.fbu.autonote.R;
import com.fbu.autonote.fragments.NotesFragment;
import com.fbu.autonote.fragments.ScanResultsFragment;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    BottomNavigationView bottomMenu;
    Fragment fragment;
    FragmentManager fragmentManager;
    Context context;
    List<ScanResult.Scan> scans;
    FirebaseFunctions firebaseFunctions;
    FirebaseAuth authManager;
    DatabaseReference databaseReference;
    String userId;
    StorageReference imageStorage;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomMenu = findViewById(R.id.menuBottomNav);
        fragmentManager = getSupportFragmentManager();
        context = this;
        authManager = FirebaseAuth.getInstance();
        userId = authManager.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference(userId);
        imageStorage = FirebaseStorage.getInstance().getReference(userId);
        firebaseFunctions = FirebaseFunctions.getInstance();

        //Init GeniusSDK
        try {
            GeniusScanSDK.init(context, getString(R.string.genius_apikey));
            Log.d("MainActivity", "GeniusSDK initialized");
        } catch (LicenseException exception) {
            Log.e("MainActivity", exception.toString());
        }

        //Set bottom menu button actions
        //TODO: PROFILE VIEW
        //TODO: NOTES VIEW
        bottomMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemNotes:
                        fragment = NotesFragment.newInstance(context);
                        break;
                    case R.id.itemScan:
                        /**
                         * Due to GeniusSDK's dependence on onActivityResult, fragment assigning
                         * gets done from withing initScanner()
                         **/
                        initScanner();
                        break;
                }
                startFragment();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            //Get results from scan and launch the scanResult fragment to upload data
            ScanResult result = ScanFlow.getScanResultFromActivityResult(data);
            scans = result.scans;
            fragment = ScanResultsFragment.newInstance(scans);
            startFragment();
            uploadImages();
        } catch (Exception e) {
            Log.e("MainActivity", e.toString());
        }
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

    private void uploadImages() {
        Toasty.info(context, "Uploading scans to the cloud", Toast.LENGTH_LONG).show();
        List<String> imagesUris = new ArrayList<>();
        String newNoteCollectionId = String.valueOf(Math.abs(new Random().nextLong()));
        StorageReference newNoteStorage = imageStorage
                .child(newNoteCollectionId);

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
                    imagesUris.add(newNoteStorage.getPath());
                }
            });
            tasks.add((Task) upload);
        }

        //Use all stored tasks in the task list and listen to when all uploads have been completed
        Task<List<Task<?>>> allUploads = Tasks.whenAllComplete(tasks);
        allUploads.addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
            @Override
            public void onSuccess(List<Task<?>> tasks) {
                Toasty.success(context, "All images uploaded!",Toasty.LENGTH_LONG).show();

                processImages(imagesUris);
            }
        });
    }

    /**
     * @param imagesUris paths on Firebase Storage that point to each individual image. Used for
     *                   Note object construction
     * @return a list of Note objects for later firebase realtime database uploading
     * @see <a href="https://firebase.google.com/docs/ml/android/recognize-text?authuser=0">
     *     Firebase Vision API docs</a>
     */
    private void processImages(List<String> imagesUris) {
        List<Task<JsonElement>> annotationTasks = new ArrayList<>();
        List<String> textContents = new ArrayList<>();

        for (ScanResult.Scan scan : scans) {
            byte[] byteArrayImage = getByteArray(scan.enhancedImageFile);
            JsonObject request = getAnnotationRequest(byteArrayImage);
            
            Task<JsonElement> annotationTask = getAnnotateImageTask(request.toString());
            //Add a failure listener to know which task failed and why
            annotationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Log.e(TAG, "Error getting transcription: "+ e.toString());
                }
            });
            annotationTasks.add(annotationTask);
        }
        //this object accumulates all tasks' results and listens to them as a single result
        Task<List<Task<JsonElement>>> allAnnotations = Tasks.whenAllSuccess(annotationTasks);
        //Wait until all tasks have been completed and then get resulting text
        allAnnotations.addOnSuccessListener(new OnSuccessListener<List<Task<JsonElement>>>() {
            @Override
            public void onSuccess(List<Task<JsonElement>> tasks) {
                //Add each task's (image's) text to an arraylist
                for (Object rawObject : tasks) {
                    JsonArray json = (JsonArray) rawObject;
                    JsonObject annotation = json.get(0)
                            .getAsJsonObject()
                            .get("fullTextAnnotation")
                            .getAsJsonObject();
                    String text = annotation.get("text").toString();
                    Log.d(TAG, String.format("%s%n", text));
                    System.out.format("%s%n", annotation.get("text").getAsString());
                    textContents.add(text);
                }
                getTopics(textContents);
            }
        });

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

    //Calls an API to get the topic of a specific block of text
    public void getTopics(List<String> detectedTexts) {
        List<String> topics = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        JSONArray texts = new JSONArray();
        for (String item : detectedTexts) {
            texts.put(item);
        }
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("texts", texts);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, bodyJson.toString());
        Request request = new Request.Builder()
                .url("https://api.uclassify.com/v1/uClassify/Topics/classify")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Token " + getString(R.string.uclassify_readkey))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String data = response.body().string();

                    //Iterate over results gotten for each text provided to the API
                    //More info about the API here: https://www.uclassify.com/docs/restapi
                JSONArray textsResults = null;
                try {
                    textsResults = new JSONArray(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, textsResults.toString());

                    for (int j = 0; j<textsResults.length(); j++) {
                        JSONArray classifications = null;
                        try {
                            classifications = textsResults.getJSONObject(j).getJSONArray("classification");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //Iterate over each topic and extract the one with the most weight
                        double max = 0;
                        String maxTopic = new String();
                        for (int i=0; i<classifications.length(); i++) {
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
                        topics.add(maxTopic);
                    }
                    for (String topic : topics) {
                        Log.d(TAG, "Topic: " + topic);
                    }
                }
        });
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
}
