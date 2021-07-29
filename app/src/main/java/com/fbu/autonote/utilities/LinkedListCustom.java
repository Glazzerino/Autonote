package com.fbu.autonote.utilities;

import android.util.Log;

import com.fbu.autonote.models.CustomNode;

import java.util.HashMap;
import java.util.LinkedList;

public class LinkedListCustom<T> {
    /**
     * @class simple implementation of a doubly linked list that returns references to node when adding items
     */
    private CustomNode<T> front;
    private CustomNode<T> back;
    public static final String TAG = "LinkedListCustom";
    int size;

    public LinkedListCustom() {
        front = null;
        back = null;
        size = 0;
    }

    public CustomNode<T> add(T data) {
        CustomNode node = new CustomNode(data);
        if (front == null) {
            this.front = node;
        } else {
            back.next = node;
            node.prev = this.back;
        }
        back = node;
        size++;
        return this.back;
    }

    public void deleteFront() {
        front = front.next;
        front.prev = null;
    }

    //Assumes node is present in list
    public void moveToBack(CustomNode<T> node) throws NullPointerException {
        if (node != back && node != front) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            back.next = node;
            node.prev = back;
            node.next = null;
        } else if (node == front) {
           back.next = node;
           node.prev = back;
           front = node.next;
           front.prev = null;
           node.next = null;
        }
        this.back = node;
    }

    public CustomNode<T> getBack() {
        return this.back;
    }

    public CustomNode<T> getFront() {
        return this.front;
    }
}
