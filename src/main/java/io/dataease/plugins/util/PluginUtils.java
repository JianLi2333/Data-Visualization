package io.dataease.plugins.util;

import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import io.dataease.plugins.common.dto.PluginSysMenu;
import io.dataease.plugins.common.service.PluginMenuService;
import io.dataease.plugins.config.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PluginUtils {


    //这些个实例都是通过set方法注入的
    private static DefaultLicenseService defaultLicenseService;


    @Autowired
    public void setDefaultLicenseService(DefaultLicenseService defaultLicenseService) {
        PluginUtils.defaultLicenseService = defaultLicenseService;
    }


    /*
     * @Author @Lijian
     * @Description //这个方法就是获取插件菜单的方法
     * @Date
     * @return
     **/

    public static List<PluginSysMenu> pluginMenus() {

        //获取当前的许可证
        F2CLicenseResponse f2CLicenseResponse = currentLic();
        //如果当前的许可证的状态不是有效的，就直接返回一个空的list
        if (f2CLicenseResponse.getStatus() != F2CLicenseResponse.Status.valid)
            return new ArrayList<>();
        //获取当前的spring容器中的所有的PluginMenuService的实例
        Map<String, PluginMenuService> pluginMenuServiceMap = SpringContextUtil.getApplicationContext().getBeansOfType(PluginMenuService.class);
        //将所有的PluginMenuService的实例中的menus方法返回的list合并成一个list
        List<PluginSysMenu> menus = pluginMenuServiceMap.values().stream().flatMap(item -> item.menus().stream()).collect(Collectors.toList());
        return menus;
    }

    /*
     * @Author @Lijian
     * @Description //这个方法就是获取当前的许可证的方法
     * @Date
     * @return
     **/

    public static F2CLicenseResponse currentLic() {
        //获取当前的环境变量
        Environment environment = SpringContextUtil.getBean(Environment.class);
        //获取当前的环境变量中的dataease.need_validate_lic的值，如果没有就是true
        Boolean need_validate_lic = environment.getProperty("dataease.need_validate_lic", Boolean.class, true);

        //如果不需要验证许可证，就直接返回一个有效的许可证
        if (!need_validate_lic) {
            F2CLicenseResponse f2CLicenseResponse = new F2CLicenseResponse();
            f2CLicenseResponse.setStatus(F2CLicenseResponse.Status.valid);
            return f2CLicenseResponse;
        }
        F2CLicenseResponse f2CLicenseResponse = defaultLicenseService.validateLicense();
        return f2CLicenseResponse;
    }




}
