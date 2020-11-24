package com.esdemo.mapper;

import com.esdemo.bean.EsBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BookMapper
 *
 * @author chendezhi
 * @date 2020/11/5 10:14
 * @since 1.0.0
 */
@Mapper
@Component
public interface BookMapper {

    List<EsBook> selectBook();

}
