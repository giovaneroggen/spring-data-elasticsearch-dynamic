package org.springframework.data.elasticsearch.core.mapping;

import org.springframework.data.elasticsearch.core.mapping.exception.IndexNotFoundException;
import org.springframework.data.elasticsearch.core.mapping.exception.TypeNotFoundException;
import org.springframework.data.elasticsearch.repository.support.DynamicIndex;
import org.springframework.data.elasticsearch.repository.support.DynamicType;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created by giovane.silva on 02/10/2017.
 */
public class DynamicIndexAndTypeContextHolder {

    private static final DynamicIndexAndTypeContextHolder INSTANCE = new DynamicIndexAndTypeContextHolder();
    public static final String ORGANIZATION_ID = "organizationId";
    public static final String STORE_ID = "storeId";

    private final ThreadLocal<DynamicIndexAndType> holder = new InheritableThreadLocal<>();

    private DynamicIndexAndTypeContextHolder() {
    }

    public static DynamicIndexAndTypeContextHolder getInstance() {
        return INSTANCE;
    }

    public <T> boolean isDynamicIndex(Class<T> javaType) {
        boolean isDynamicIndex = javaType.isAnnotationPresent(DynamicIndex.class) && holder.get() != null;
        return isDynamicIndex && dynamictIndexIsPresent();
    }

    public <T> boolean isDynamicType(Class<T> javaType) {
        boolean isDynamicType = this.isDynamicIndex(javaType) && javaType.isAnnotationPresent(DynamicType.class);
        return isDynamicType && dynamictTypeIsPresent();
    }

    private boolean dynamictIndexIsPresent() {
        if(isBlank(holder.get().index)){
            throw new IndexNotFoundException("INDEX_NOT_PRESENT");
        }
        return true;
    }

    private boolean dynamictTypeIsPresent() {
        if(isBlank(holder.get().type)){
            throw new TypeNotFoundException("TYPE_NOT_PRESENT");
        }
        return true;
    }

    public String getIndex(String value) {
        return value + "-" + holder.get().index;
    }

    public String getType(String value) {
        return value + "-" + holder.get().type;
    }

    public void setIndexAndType(String index, String type) {
        DynamicIndexAndType holder = new DynamicIndexAndType();
        holder.index = index;
        holder.type = type;
        this.holder.set(holder);
    }

    public void clean() {
        holder.remove();
    }

    public class DynamicIndexAndType {
        public String index;
        public String type;
    }

}
