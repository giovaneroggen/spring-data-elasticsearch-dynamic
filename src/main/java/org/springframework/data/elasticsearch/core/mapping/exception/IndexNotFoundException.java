package org.springframework.data.elasticsearch.core.mapping.exception;

/**
 * Created by giovane.silva on 02/10/2017.
 */
public class IndexNotFoundException extends RuntimeException {

    public IndexNotFoundException(String message){
        super(message);
    }
}
