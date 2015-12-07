package bfbc.photolib;

import bfbc.photolib.Heap.ClientUpdateRequest;

public interface HeapChangeListener {
	void reportChange(ClientUpdateRequest cr);
}
