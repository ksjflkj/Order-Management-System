package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewVO {
    private Long id;
    private Long orderId;
    private Long merchantId;
    private String shopName;
    private String nickname;
    private String avatar;
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
}
