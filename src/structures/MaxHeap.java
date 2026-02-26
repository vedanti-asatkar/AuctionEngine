package structures;

import model.Bid;
import java.util.ArrayList;

public class MaxHeap {

    private ArrayList<Bid> heap;

    public MaxHeap() {
        heap = new ArrayList<>();
    }

    public void insert(Bid bid) {
        heap.add(bid);
        heapifyUp(heap.size() - 1);
    }

    public Bid getMax() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;

            if (heap.get(index).getAmount() > heap.get(parent).getAmount()) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        Bid temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}