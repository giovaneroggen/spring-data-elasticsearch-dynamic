package org.springframework.data.elasticsearch;

import org.elasticsearch.index.IndexNotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.mapping.DynamicIndexAndTypeContextHolder;
import org.springframework.data.elasticsearch.entities.DynamicIndexAndType;
import org.springframework.data.elasticsearch.repositories.dynamic.DynamicIndexAndTypeRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

/**
 * Created by giovane.silva on 02/10/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/repository-test-dynamic-index-type.xml")
public class DynamicIndexAndTypeTests {

    @Autowired
    private DynamicIndexAndTypeRepository repository;

    @After
    public void after(){
        DynamicIndexAndTypeContextHolder.getInstance().clean();
    }

    @Test
    public void test(){
        DynamicIndexAndType test = new DynamicIndexAndType(1L, "NAME 1");
        DynamicIndexAndType test2 = new DynamicIndexAndType(2L, "NAME 2");

        this.setIndexAndType("index", null);

        System.out.println("saving objects with index=test-dynamic-index and type=default");
        this.repository.saveAll(Arrays.asList(test, test2));

        this.setIndexAndType("index2", null);

        System.out.println("saving objects with index=test-dynamic-index2 and type=default");
        this.repository.saveAll(Arrays.asList(test, test2));

        this.setIndexAndType("index", null);

        System.out.println("searching objects with index=test-dynamic-index and type=default");
        Assert.assertTrue(this.repository.findAll(new PageRequest(0, 1000)).getTotalElements() == 2);

        this.setIndexAndType("index2", null);

        System.out.println("searching objects with index=test-dynamic-index2 and type=default");
        Assert.assertTrue(this.repository.findAll(new PageRequest(0, 1000)).getTotalElements() == 2);
    }

    private void setIndexAndType(String index, String type) {
        DynamicIndexAndTypeContextHolder.getInstance().setIndexAndType(index, type);
    }
}
