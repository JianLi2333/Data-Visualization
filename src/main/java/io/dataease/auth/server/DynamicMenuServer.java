package io.dataease.auth.server;

import io.dataease.auth.api.DynamicMenuApi;
import io.dataease.auth.api.dto.DynamicMenuDto;
import io.dataease.auth.service.DynamicMenuService;
import io.dataease.commons.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @Author @Lijian
 * @Description //这个类就有点像Controller,他是api文件夹中的DynamicMenuApi接口的实现类,
 * 然后mapping的路径写在了接口上,实现类上没有写的话,就会默认使用接口上的路径
 * @Date 2020/7/15 16:04
 * @return
 **/

@RestController
public class DynamicMenuServer implements DynamicMenuApi {
    @Autowired
    private DynamicMenuService dynamicMenuService;

    @Override
    public List<DynamicMenuDto> menus() {
        ServletUtils.getToken();
        return dynamicMenuService.load(null);
    }
}
