package org.optipace.authmanager.security;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.optipace.authmanager.Entity.Permission;
import org.optipace.authmanager.Entity.Role;
import org.optipace.authmanager.Entity.User;
import org.optipace.authmanager.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthorityCacheService {

    private  final UserRepository userRepository;


            private Cache<Long, Set<GrantedAuthority>> cacheAuthority =
                    Caffeine.newBuilder()
                            .expireAfterWrite(30 , TimeUnit.MINUTES)
                            .maximumSize(10000)
                            .build();

      public Set<GrantedAuthority> getAuthority(Long userId){


          return cacheAuthority.get(userId,this::loadAuthorities);
      }



    private Set<GrantedAuthority> loadAuthorities(Long userId){


          User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User Not Found"));

          Set<GrantedAuthority> authorities = new HashSet<>();

          Role role = user.getRole();

          if(role!=null){


              authorities.add(new SimpleGrantedAuthority( "ROLE_" + role.getName()));
          }

          if(role.getPermissions()!=null){


              for(Permission permission : role.getPermissions()){

                  authorities.add(new SimpleGrantedAuthority( permission.getName()));
              }
          }
          return authorities;

    }



}
