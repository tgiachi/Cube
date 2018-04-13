package com.github.tgiachi.cubemediaserver.entities;

import com.github.tgiachi.cubemediaserver.entities.base.BaseEntity;
import lombok.*;

import org.springframework.util.DigestUtils;


@EqualsAndHashCode(callSuper=false)
@ToString
public class UserEntity extends BaseEntity {

    @Getter @Setter
    String email;

    @Getter @Setter
    boolean isActive;

    @Getter
    String passwordMd5;

    public void setPasswordMd5(String password)
    {
        passwordMd5 = DigestUtils.md5DigestAsHex(password.getBytes());
    }

    public boolean isPasswordEquals(String password)
    {
        return passwordMd5.equals(DigestUtils.md5DigestAsHex(password.getBytes()));
    }

    public UserEntity()
    {
        isActive = true;
    }
}
