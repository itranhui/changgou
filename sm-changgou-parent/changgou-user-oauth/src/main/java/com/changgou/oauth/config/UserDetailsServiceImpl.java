package com.changgou.oauth.config;

import com.changgou.oauth.util.UserJwt;
import com.changgou.user.feign.UserFeign;
import com.changgou.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*****
 * 自定义授权认证类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ClientDetailsService clientDetailsService;

    /**
     * 注入Feign
     *
     */
    @Autowired
    private UserFeign userFeign;
    /****
     * 自定义授权认证
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //=======================客户端信息认证开始================================//

        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if (authentication == null) {
            //查询数据库
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if (clientDetails != null) {
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式
               // return new User(username, new BCryptPasswordEncoder().encode(clientSecret), AuthorityUtils.commaSeparatedStringToAuthorityList(""));
                //数据库查找方式   客户端ID    客户端密钥
                return new User(username,clientSecret, AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        //=======================客户端信息认证结束================================//

        //=======================用户信息认证开始、校验密码================================//
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        //客户端ID     :  changgou
        //客户端秘钥   :  changgou
        //普通用户->账号：任意账号   密码：szitheima
        //根据用户名查询用户信息
       // String pwd = new BCryptPasswordEncoder().encode("szitheima");
        //创建User对象
        //通过feign调用
            Result<com.changgou.user.pojo.User> userResult = userFeign.findById(username);
        if (userResult==null || userResult.getData() ==null){
            return null;
        }
        String permissions = "admin";
        UserJwt userDetails = new UserJwt(username, userResult.getData().getPassword(), AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));
        return userDetails;
        //=======================用户信息认证结束================================//
    }
}
