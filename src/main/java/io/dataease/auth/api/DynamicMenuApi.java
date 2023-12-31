package io.dataease.auth.api;


import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.api.dto.DynamicMenuDto;
import io.dataease.controller.handler.annotation.I18n;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Api(tags = "权限：动态菜单")
//这个注解是swagger的，用来分组的，如果不写，所有接口都会在default分组里
@ApiSupport(order = 20)
//这个注解也可以写在接口上，这样就不用每个方法都写了，所有实现了这个接口的类都会有这个路径
@RequestMapping("/api/dynamicMenu")
public interface DynamicMenuApi {

    /**
     * 根据heads中获取的token 获取username 获取对应权限的菜单
     * @return
     */
    @ApiOperation("查询")
    @PostMapping("/menus")
    @I18n
    List<DynamicMenuDto> menus();

}
