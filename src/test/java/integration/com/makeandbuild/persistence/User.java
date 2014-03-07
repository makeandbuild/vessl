package com.makeandbuild.persistence;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.makeandbuild.persistence.jdbc.SaveWhen;
import com.makeandbuild.persistence.jdbc.Specialize;

@Table(name = "user")
@Specialize(typeColumn = "javatype")
public class User {
    @Id
    @Column(name = "user_id")
    @SaveWhen(insert = true, update = false)
    private Long id;

    @Column(name = "username")
    @SaveWhen(insert = true, update = false)
    @NotNull
    @Size(min = 0, max = 255)
    private String username;

    @Column(name = "login_count")
    @SaveWhen(insert = true, update = true)
    @Min(0)
    private Integer loginCount;

    @Column(name = "created_at")
    @SaveWhen(insert = true, update = true)
    private Date createdAt;

    @Column(name = "user_type")
    @SaveWhen(insert = true, update = true)
    private UserType userType;

    @Column(name = "longitude")
    @SaveWhen(insert = true, update = true)
    private Double longitude;

    @Column(name = "latitude")
    @SaveWhen(insert = true, update = true)
    private Double latitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


}
