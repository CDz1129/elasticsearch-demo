package com.esdemo.dao;

import com.esdemo.bean.EsBook;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * BookRepository
 *
 * @author chendezhi
 * @date 2020/11/5 11:20
 * @since 1.0.0
 */
public interface BookRepository extends ElasticsearchRepository<EsBook,Long> {
}
