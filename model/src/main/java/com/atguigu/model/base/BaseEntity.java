package com.atguigu.model.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class BaseEntity implements Serializable {
    //@TableId注解是专门用在主键上的注解，如果数据库中的主键字段名和实体中的属性名
    @TableId(type = IdType.AUTO)
    private Long id;
    //@TableField用于其他属性所对应字段不一致的注解。
    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableLogic//逻辑删除字段，必须加，不然真删除
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private Map<String,Object> param = new HashMap<>();
}
