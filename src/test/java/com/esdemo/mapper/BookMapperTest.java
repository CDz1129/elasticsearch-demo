package com.esdemo.mapper;

import com.esdemo.EsDemoApplicationTests;
import com.esdemo.bean.EsBook;
import com.esdemo.dao.BookRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BookMapperTest
 *
 * @author chendezhi
 * @date 2020/11/5 10:27
 * @since 1.0.0
 */
@Component
@Slf4j
class BookMapperTest extends EsDemoApplicationTests {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void delData() {
        bookRepository.deleteAll();
    }

    @Test
    void saveData() {
        List<EsBook> list = bookMapper.selectBook();
        Iterable<EsBook> esBooks = elasticsearchRestTemplate.save(list);
        log.info("【book】=={}", esBooks);
    }

    @Test
    void update() {
        bookRepository.findById(1001L).ifPresent(e -> {
            e.setBookname("修改过得" + e.getBookname());
            EsBook save = bookRepository.save(e);
            log.info("【update book】={}", save);
        });
    }

    @Test
    void get() throws IOException {
        EsBook esBook = elasticsearchRestTemplate.get("1001", EsBook.class);
        log.info("【elasticsearchRestTemplate.get】={}", esBook);
    }

    @SneakyThrows
    @Test
    void select() {
        String keyWord = "嫡女";
//        NativeSearchQuery query = new NativeSearchQuery(QueryBuilders.multiMatchQuery("嫡女", "bookname", "desc", "tags", "author"));
        NativeSearchQuery build = new NativeSearchQueryBuilder().withQuery(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("bookname", keyWord).boost(1f))
                .should(QueryBuilders.matchQuery("desc", keyWord).boost(1f))
                .should(QueryBuilders.matchQuery("tags", keyWord).boost(1f))
                .should(QueryBuilders.matchQuery("author", keyWord).boost(1f)))
                .withHighlightFields(
                        new HighlightBuilder.Field("bookname"),
                        new HighlightBuilder.Field("desc"),
                        new HighlightBuilder.Field("tags"),
                        new HighlightBuilder.Field("author")
                ).build();
        SearchHits<EsBook> search = elasticsearchRestTemplate.search(build, EsBook.class);
        List<EsBook> collect = search.stream().map(e -> e.getContent()).collect(Collectors.toList());
        log.info("【select:esBook】={}", search);
    }

    @Test
    void selectWithWeight() {
        String keyWord = "嫡女";

        NativeSearchQuery build = new NativeSearchQueryBuilder().withQuery(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("bookname", keyWord).boost(1f))
                .should(QueryBuilders.matchQuery("desc", keyWord).boost(0.5f))
                .should(QueryBuilders.matchQuery("tags", keyWord).boost(1f))
                .should(QueryBuilders.matchQuery("author", keyWord).boost(1f)))
                .withHighlightFields(
                        new HighlightBuilder.Field("bookname"),
                        new HighlightBuilder.Field("desc"),
                        new HighlightBuilder.Field("tags"),
                        new HighlightBuilder.Field("author")
                        ).build();
        SearchHits<EsBook> search = elasticsearchRestTemplate.search(build, EsBook.class);
        List<EsBook> collect = search.stream().map(e -> e.getContent()).collect(Collectors.toList());
        log.info("【selectWithWeight:esBook】={}", search);
        select();
    }

    @Test
    void functionQuery() {
        String keyWord = "总裁";
        Script script = new Script(" 1.2 * doc['heats'].value + Math.log(2+doc['totalsubscribes'].value+doc['totalreadtimes'].value + doc['totalvotes'].value)");
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("bookname",keyWord).boost(100f).slop(2))
                        .should(QueryBuilders.matchQuery("bookname", keyWord).boost(20f))
                        .should(QueryBuilders.matchPhraseQuery("desc",keyWord).boost(2f).slop(2))
                        .should(QueryBuilders.matchQuery("desc", keyWord).boost(5f))
                        .should(QueryBuilders.matchQuery("tags", keyWord).boost(1f))
                        .should(QueryBuilders.matchQuery("author", keyWord).boost(1f))
                        .minimumShouldMatch(1),
                ScoreFunctionBuilders.scriptFunction(script));

        NativeSearchQuery build = new NativeSearchQueryBuilder().withQuery(functionScoreQueryBuilder)
                .withHighlightFields(
                        new HighlightBuilder.Field("bookname"),
                        new HighlightBuilder.Field("desc"),
                        new HighlightBuilder.Field("tags"),
                        new HighlightBuilder.Field("author")
                ).withPageable(PageRequest.of(0,5))
                .build();

        SearchHits<EsBook> search = elasticsearchRestTemplate.search(build, EsBook.class);

        List<EsBook> collect = search.stream().map(e -> e.getContent()).collect(Collectors.toList());
        log.info("【functionQuery:esBook】={},size={}", collect,collect.size());
    }

    @Test
    void getMapping() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(EsBook.class);
        Map<String, Object> mapping = indexOperations.getMapping();
        log.info("【book-mapping】={}",mapping);
    }

    @Test
    void updateMappingWeight() {
        getMapping();
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(EsBook.class);
        Map<String, Object> mapping = indexOperations.getMapping();
        Map<String, Object> properties = (Map)mapping.get("properties");
        Map<String, Object> bookname = (Map)properties.get("bookname");
        Map<String, Object> tags = (Map)properties.get("tags");
        tags.put("boost",0.9f);
        Map<String, Object> author = (Map)properties.get("author");
        author.put("boost",1f);
        Document document = Document.from(mapping);
        indexOperations.putMapping(document);

        System.out.println(document.toJson());
        getMapping();
    }
}