package net.skydistrict.claimsgui.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexedArray {
    private Object[] array;

    public IndexedArray(Object[] array) {
        this.array = array;
    }

    public int indexOf(Object object) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == object) return i;
        }
        return -1;
    }

    public boolean set(int index, Object object) {
        if (index < array.length - 1) {
            array[index] = object;
            return true;
        }
        return false;
    }

    public Object get(int index) {
        return array[index];
    }

    public List<Object> toList() {
        return Arrays.asList(array);
    }
}
