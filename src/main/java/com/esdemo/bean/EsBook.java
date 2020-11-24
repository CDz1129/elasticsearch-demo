package com.esdemo.bean;

import com.esdemo.EsConsts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;

/**
 * EsBook
 *
 * @author chendezhi
 * @date 2020/11/4 16:21
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = EsConsts.INDEX_NAME, shards = 1, replicas = 0)
public class EsBook implements Serializable {

    @Id
    private Long bookid;
    @Field(type = FieldType.Keyword)    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"), otherFields = {
            @InnerField(type = FieldType.Keyword, suffix = "keyword")
    })
    private String bookname;

    private String author;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String desc;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String tags;
    //总订阅数
    @Field(type = FieldType.Integer)
    private Integer totalsubscribes;
    //总阅读数
    @Field(type = FieldType.Integer)
    private Integer totalreadtimes;
    //总投票数
    @Field(type = FieldType.Integer)
    private Integer totalvotes;
    //热度
    @Field(type = FieldType.Integer)
    private Integer heats;
}
