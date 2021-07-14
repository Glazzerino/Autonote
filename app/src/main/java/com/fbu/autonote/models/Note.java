package com.fbu.autonote.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * Represents the data model that contains all note-related data.
 * These values match those of their firebase counterparts.
 * @note: This class is intended for runtime data representaton only. Storage should be managed by
 * the Firebase realtime database interface
 * @author: Francisco Zampora
 * @param topic this value should be filled with the result of the uClassify API.
 * @param keywords contains words intended to provide a general context of the contents inside the note
 * @param imageURL points to the firebase database file that contains the actual image of the source material
 * @param textContent stores the Optical Character Recognition result from the source image (imageURL)
 * @param noteId unique identifier of the note
 * @param userId points to the user that owns this notebook
 */
public class Note {
    private String topic;
    private List<String> keywords;
    private String imageURL;
    private String textContent;
    private long noteId;
    private long userId;

    public Note(String topic, List<String> keywords, String imageURL, String textContent, long noteId, long userId) {
        this.topic = topic;
        this.keywords = keywords;
        this.imageURL = imageURL;
        this.textContent = textContent;
        this.noteId = noteId;
        this.userId = userId;
    }

    public Note() { }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    public long getNoteId() {
        return noteId;
    }

    public long getUserId() {
        return userId;
    }

    public JSONObject asJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("userId", userId);
        jsonObject.put("noteId", noteId);
        jsonObject.put("imageUrl", imageURL);
        jsonObject.put("keywords", keywords);
        jsonObject.put("textContent", textContent);
        return jsonObject;
    }
}
