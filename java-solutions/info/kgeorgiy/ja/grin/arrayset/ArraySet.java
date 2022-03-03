package info.kgeorgiy.ja.grin.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {

    private final List<T> data;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this.data = Collections.emptyList();
        this.comparator = null;
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends T> inputData) {
        this(inputData, null);
    }

    public ArraySet(Collection<? extends T> inputData, Comparator<? super T> comparator) {
        TreeSet<T> tmp = new TreeSet<>(comparator);
        tmp.addAll(inputData);
        this.data = new ArrayList<>(tmp);
        this.comparator = comparator;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (isEmpty() || compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
        return new ArraySet<>(data.subList(getIndex(fromElement), getIndex(toElement)), comparator);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        if (isEmpty() || compare(first(), toElement) > 0) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(data.subList(0, getIndex(toElement)), comparator);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        if (isEmpty() || compare(fromElement, last()) > 0) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(data.subList(getIndex(fromElement), size()), comparator);
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(size() - 1);
    }

    @SuppressWarnings("unchecked")
    private int compare(T t1, T t2) {
        return comparator() == null ? ((Comparable<? super T>) t1).compareTo(t2) : comparator().compare(t1, t2);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) o, comparator) > -1;
    }

    private int getIndex(T t) {
        int i = Collections.binarySearch(data, t, comparator);
        return (i < 0 ? -(i + 1) : i);
    }




    public static void main(String[] args) {
        SortedSet<Integer> set = new ArraySet<>(List.of(1, 2, 4, 7));
        System.out.println(set.subSet(8, -1));
    }
}


