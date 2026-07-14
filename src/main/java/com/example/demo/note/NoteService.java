package com.example.demo.note;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NoteService {

	private final Map<Long, Note> notes = new ConcurrentHashMap<>();
	private final AtomicLong nextId = new AtomicLong(1);

	public List<Note> findAll() {
		return new ArrayList<>(notes.values());
	}

	public Note findById(Long id) {
		Note note = notes.get(id);
		if (note == null) {
			throw new NoSuchElementException("Note not found: " + id);
		}
		return note;
	}

	public Note create(NoteRequest request) {
		Instant now = Instant.now();
		Long id = nextId.getAndIncrement();
		Note note = new Note(id, request.title(), request.content(), now, now);
		notes.put(id, note);
		return note;
	}

	public Note update(Long id, NoteRequest request) {
		Note existing = findById(id);
		Note updated = new Note(id, request.title(), request.content(), existing.createdAt(), Instant.now());
		notes.put(id, updated);
		return updated;
	}

	public void delete(Long id) {
		if (notes.remove(id) == null) {
			throw new NoSuchElementException("Note not found: " + id);
		}
	}
}
