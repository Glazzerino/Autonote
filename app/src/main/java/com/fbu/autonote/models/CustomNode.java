package com.fbu.autonote.models;

public class CustomNode<T> {
    public T data;
    public CustomNode<T> next;
    public CustomNode<T> prev;

    public CustomNode(T data, CustomNode<T> next, CustomNode<T> prev) {
        this.data = data;
        this.next = next;
        this.prev = prev;
    }

    public CustomNode(T data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }

}
