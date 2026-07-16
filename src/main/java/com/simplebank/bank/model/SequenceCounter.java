package com.simplebank.bank.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB counter document used to generate readable account numbers.
 */
@Document("counters")
public class SequenceCounter {

	@Id
	private String id;

	private long sequence;

	public SequenceCounter() {
	}

	public long getSequence() {
		return sequence;
	}
}
