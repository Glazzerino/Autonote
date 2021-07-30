package com.fbu.autonote.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Note implements Parcelable {
    /**
     * Represents the data model that contains all note-related data.
     * These values match those of their firebase counterparts.
     *
     * @note: This class is intended for runtime data representaton only. Storage should be managed by
     * the Firebase realtime database interface
     * @author: Francisco Zampora
     * @param topic this value should be filled with the result of the uClassify API.
     * @param keywords contains words intended to provide a general context of the contents inside the note
     * @param imageURL points to the firebase database file that contains the actual image of the source material
     * @param textContent stores the Optical Character Recognition result from the source image (imageURL)
     * @param noteId unique identifier of the note
     * @param date date of creation
     */
    private String topic;
    private List<String> keywords;
    private String imageURL;
    private String textContent;
    private String noteId;
    private String date;
    private String url;

    public Note() {
        keywords = new ArrayList<>();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(topic);
        dest.writeString(textContent);
        dest.writeString(imageURL);
        dest.writeString(noteId);
        dest.writeString(date);
        dest.writeString(url);
        dest.writeStringList(keywords);
    }

    protected Note(Parcel parcel) {
        this.topic = parcel.readString();
        this.textContent = parcel.readString();
        this.imageURL = parcel.readString();
        this.noteId = parcel.readString();
        this.date = parcel.readString();
        this.url = parcel.readString();
        keywords = new ArrayList<>(10);
        parcel.readStringList(keywords);
    }

    public Note(String topic, String textContent, String imageURL, String noteId, String date, String url, List<String> keywords) {
        this.topic = topic;
        this.textContent = textContent;
        this.imageURL = imageURL;
        this.noteId = noteId;
        this.date = date;
        this.url = url;
        this.keywords = new ArrayList<>(keywords);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void addKeyword(String word) {
        keywords.add(word);
    }

    public static Note fromDataSnapshot(DataSnapshot snapshot) {
        Note note = new Note();
        note.setTopic(snapshot.child("topic").getValue(String.class));
        note.setNoteId(snapshot.child("noteId").getValue(String.class));
        note.setTextContent(snapshot.child("textContent").getValue(String.class));
        note.setUrl(snapshot.getRef().getPath().toString());
        for (DataSnapshot keyword : snapshot.child("keywords").getChildren()) {
            note.keywords.add(keyword.getValue(String.class));
        }
        note.setImageURL(snapshot.child("imageURL").getValue(String.class));
        note.setDate(snapshot.child("date").getValue(String.class));
        return note;
    }

    public void setKeywords(List<String> keywords) {
        //Copies data instead of getting reference
        this.keywords = new ArrayList<>(keywords);
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getTextContent() {
        return textContent;
    }

    public String getNoteId() {
        return noteId;
    }


    public JSONObject asJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("noteId", noteId);
        jsonObject.put("imageUrl", imageURL);
        jsonObject.put("keywords", keywords);
        jsonObject.put("textContent", textContent);

        return jsonObject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
