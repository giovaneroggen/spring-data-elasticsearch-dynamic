package org.springframework.data.elasticsearch.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.support.DynamicIndex;
import org.springframework.data.elasticsearch.repository.support.DynamicType;

/**
 * Created by giovane.silva on 02/10/2017.
 */

/**
 * createIndex = false
 */
@DynamicIndex
@Document(indexName = "test-dynamic", type = "test-type-dynamic", createIndex = false)
public class DynamicIndexAndType {

    @Id
    private Long id;
    private String name;

    public DynamicIndexAndType(){}

    public DynamicIndexAndType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
