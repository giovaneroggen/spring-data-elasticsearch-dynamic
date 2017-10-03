Spring Data Elasticsearch
=========================

Spring Data implementation for ElasticSearch Dynamic

Spring Data makes it easier to build Spring-powered applications and this project is a extension of spring-data-elasticsearch, in order to promote the functionality of an entity having the dynamic "indexName" and "type" improving the performance of your search in different contexts

Example
-----------

##### Product
- Belongs to one organization
- ``organization == "indexName"``
```
@DynamicIndex
@Document(indexName = "product", type = "product", createIndex = false)
class Product{
    @Id
    private Long id;
    private String name;
}
```
##### ProductConfiguration
- The ProductConfiguration is a configuration for each store(price, stock), belonging to a store and one organization
-    ``store = "type" ``
-    ``organization == "indexName"``
```
@DynamicIndex
@DynamicType
@Document(indexName = "product-configuration", type = "product-configuration", createIndex = false)
public class ProductConfiguration{
    @Id
    private Long id;
    private Long stock;
    private Double price;
    private Long productId;
}
```

####  When a class is annotated with @DynamicIndex/@DynamicType, it can only be used if the index/type is defined in context
##### Use this code below in order to set wich indexName(organization) and type(store) you are working
*  DynamicIndexAndTypeContextHolder.getInstance().setIndexAndType(`organizationId`, `storeId`); 

##### Or else you will get these exceptions
[IndexNotFoundException](src/main/java/org/springframework/data/elasticsearch/core/mapping/exception/IndexNotFoundException.java)
[TypeNotFoundException](src/main/java/org/springframework/data/elasticsearch/core/mapping/exception/TypeNotFoundException.java)

[Test Code Example](src/test/java/org/springframework/data/elasticsearch/DynamicIndexAndTypeTests.java) 