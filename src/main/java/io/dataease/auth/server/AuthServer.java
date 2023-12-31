package io.dataease.auth.server;

import io.dataease.auth.api.AuthApi;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.dataease.auth.config.RsaProperties;
import io.dataease.auth.filter.entity.SysUserEntity;
import io.dataease.auth.filter.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.RsaUtil;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;

import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthServer implements AuthApi {

    @Autowired
    private AuthUserService authUserService;


    @Override
    public Object login(@RequestBody LoginDto loginDto) throws Exception {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        //根据用户名获取用户信息
        SysUserEntity user = authUserService.getUserByName(username);

        //判断用户是否存在
        if (ObjectUtils.isEmpty(user)) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        //判断用户是否被禁用
        if (user.getEnabled() == 0) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        //获取用户密码
        String realPwd = user.getPassword();
        //私钥解密,从前端传过来的密码是使用公钥加密的,这里使用私钥解密
        String pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, password);
        //将解密后的密码进行md5加密,其实pwd就是真实的密码,但是为了安全起见,还是进行了md5加密,这样就算数据库被盗了,也不会泄露用户的密码
        pwd = CodingUtil.md5(pwd);

        //判断密码是否正确
        if (!StringUtils.equals(pwd, realPwd)) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        Map<String, Object> result = new HashMap<>();
        //这一步就是生成一个tokeninfo对象,里面存放了用户的id和用户名
        TokenInfo tokenInfo = TokenInfo.builder().userId(user.getUserId()).username(username).build();
        //然后使用用户的密码进行加密生成JWT令牌，也就是token,这样就算token被截获了,也无法解密出用户的信息
        String token = JWTUtils.sign(tokenInfo, realPwd);
        // 记录token操作时间
        result.put("token", token);
        //将token放入到响应头中
        /*在 Web 应用中，JWT 令牌通常用于身份验证和会话管理。当用户登录后，服务器会生成一个包含用户信息的 JWT 令牌，
        并将其发送给客户端。客户端在后续的请求中会将这个令牌附加在 HTTP 请求头中，服务器通过验证这个令牌来确认用户的身份。
        在这个场景中，ServletUtils.setToken(token); 将 JWT 令牌设置到 HTTP 响应头中，这样客户端在接收到响应后可以从响应头中获取到这个令牌。
        然后客户端在后续的请求中会将这个令牌附加在请求头中，以此来证明自己的身份。*/
        ServletUtils.setToken(token);
        return result;
    }

    @Override
    public CurrentUserDto userInfo() {
        CurrentUserDto userDto = (CurrentUserDto) SecurityUtils.getSubject().getPrincipal();
        if (ObjectUtils.isEmpty(userDto)) {
            // 从响应头中获取token
            String token = ServletUtils.getToken();
            // 从token中获取用户id
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            // 根据用户id获取用户信息
            SysUserEntity user = authUserService.getUserById(userId);
            // 将用户信息转换为CurrentUserDto对象
            CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
            // 获取用户角色信息
            List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
            // 获取用户权限信息
            List<String> permissions = authUserService.permissions(user.getUserId());
            // 将用户角色信息和权限信息放入到CurrentUserDto对象中
            currentUserDto.setRoles(currentRoleDtos);
            currentUserDto.setPermissions(permissions);
            return currentUserDto;
        }
        return userDto;
    }

    @Override
    public String logout() {
        String token = ServletUtils.getToken();
        if (StringUtils.isEmpty(token) || StringUtils.equals("null", token) || StringUtils.equals("undefined", token)) {
            return "success";
        }
        try{
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            authUserService.clearCache(userId);
        }catch (Exception e) {
            LogUtil.error(e);
            return "fail";
        }

        return "success";
    }

    @Override
    public Boolean validateName(@RequestBody Map<String, String> nameDto) {
        String userName = nameDto.get("userName");
        if (StringUtils.isEmpty(userName)) return false;
        SysUserEntity userEntity = authUserService.getUserByName(userName);
        if (ObjectUtils.isEmpty(userEntity)) return false;
        return true;
    }

    /*@Override
    public Boolean isLogin() {
        return null;
    }*/


}
