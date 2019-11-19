package net.htwater.sesame.dms.service.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jokki
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 7129590002905578982L;

    @Id
    private String id;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;


}
