package org.springframework.data.elasticsearch.core.completion;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * @author Franck Marchand
 * @author Mohsin Husen
 * @author Mewes Kochheim
 */
@Document(indexName = "test-index-completion-annotated", type = "completion-annotation-type", shards = 1, replicas = 0, refreshInterval = "-1")
public class CompletionAnnotatedEntity {

	@Id
	private String id;
	private String name;

	@CompletionField
	private Completion suggest;

	private CompletionAnnotatedEntity() {
	}

	public CompletionAnnotatedEntity(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Completion getSuggest() {
		return suggest;
	}

	public void setSuggest(Completion suggest) {
		this.suggest = suggest;
	}
}
