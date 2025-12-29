package com.ddl.manager.shared.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 基础实体类
 * 抽取公共字段：id、uuid、createTime、updateTime
 * @author 郑海培
 * @since 2025-12-13
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    /** 内部自增主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 对外暴露的唯一标识 */
    @Column(unique = true, nullable = false, length = 36, updatable = false)
    private String uuid;

    /** 创建时间 */
    @Column(updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.createTime == null) {
            this.createTime = now;
        }
        this.updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
