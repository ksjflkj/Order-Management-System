package org.example.ordermanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Integer rating;
    private String content;
    /** 商家回复 */
    private String merchantReply;
    private LocalDateTime merchantReplyTime;
    /** 用户追评 */
    private String followUpContent;
    private LocalDateTime followUpTime;
    /** 状态：1正常，0屏蔽 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}