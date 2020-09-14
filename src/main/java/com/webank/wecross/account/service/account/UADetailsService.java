package com.webank.wecross.account.service.account;

import com.webank.wecross.account.service.exception.AccountManagerException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class UADetailsService implements UserDetailsService {
    private static Logger logger = LoggerFactory.getLogger(UADetailsService.class);

    private UAManager uaManager;

    public UADetailsService(UAManager uaManager) {
        this.uaManager = uaManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        try {
            UniversalAccount ua = uaManager.getUA(username);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            authorities.add(new SimpleGrantedAuthority("ROLE_" + ua.getRole()));

            // TODO: encrypt password plain text
            return new User(ua.getUsername(), "{noop}" + ua.getPassword(), authorities);
        } catch (AccountManagerException e) {
            logger.error("load user exception: " + e.getMessage());
            return null;
        }
    }
}
