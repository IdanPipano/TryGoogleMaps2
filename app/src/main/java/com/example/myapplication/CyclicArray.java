package com.example.myapplication;

public class CyclicArray<T> {
    private T[] array;
    private int size;
    private int head;

    public CyclicArray(int capacity) {
        array = (T[]) new Object[capacity];
        size = 0;
        head = 0;
    }

    public T[] getArray() {
        return array;
    }

    public void add(T element) {
        array[head] = element;
        head = (head + 1) % array.length;
        if (size < array.length) {
            size++;
        }
    }

    public T get(int index) {
        if (index >= 0 && index < size) {
            int actualIndex = (head - size + index + array.length) % array.length;
            return array[actualIndex];
        }
        throw new IndexOutOfBoundsException();
    }

    public int getHead() {
        return head;
    }

    public int getSize() {
            return size;
        }
}

