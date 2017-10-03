package org.springframework.data.elasticsearch.repositories.dynamic;

import org.springframework.data.elasticsearch.entities.DynamicIndexAndType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Created by giovane.silva on 02/10/2017.
 */
public interface DynamicIndexAndTypeRepository extends ElasticsearchRepository<DynamicIndexAndType, Long> {
}
