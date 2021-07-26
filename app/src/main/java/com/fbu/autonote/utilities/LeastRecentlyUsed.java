package com.fbu.autonote.utilities;

import com.fbu.autonote.models.CustomNode;

import java.util.HashMap;
import java.util.Iterator;

public class LeastRecentlyUsed<T> implements Iterator<T> {
    /**
     * @class This is a basic implementation of the LRU Cache Policy, which discards theitem at the front of a queue
     *  when the number of items exceeds a specified threshold
     * @field limit non-inclusive limit of items allowed to exist inside the container
     * @field pointerTable stores a pointer to the item inside the queue for fast verification of existence
     * @field next used as part of the implementation of the Iterator<T> interface
     */
    private LinkedListCustom<T> container;
    private HashMap<T, CustomNode<T>> pointerTable;
    CustomNode<T> next; //For iteration
    int limit;

    public LeastRecentlyUsed(int limit) {
        this.limit = limit;
        container = new LinkedListCustom<>();
        pointerTable = new HashMap<>(limit, 0.8f);
        next = null;
    }

    public void update(T data) {
        if (pointerTable.containsKey(data)) {
            container.moveToBack(pointerTable.get(data));
            pointerTable.replace(data, container.getBack());
        } else {
            container.add(data);
            pointerTable.put(data, container.getBack());
            //Maintain queue size
            if (container.size > limit) {
                pointerTable.remove(container.getFront().data);
                container.deleteFront();
            }
        }
        this.next = container.getFront();
    }

    @Override
    public boolean hasNext() {
        if (next.next != null) {
            next = next.next;
            return true;
        }
        return false;
    }

    @Override
    public T next() {
        return next.data;
    }
}
