package com.webank.wecross.account.service.account;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UADetailsService implements UserDetailsService {
    private UAManager uaManager;

    public UADetailsService(UAManager uaManager) {
        this.uaManager = uaManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UniversalAccount ua = uaManager.getUA(username);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + ua.getRole()));

        // TODO: encrypt password plain text
        return new User(ua.getUsername(), "{noop}" + ua.getPassword(), authorities);
    }
}
