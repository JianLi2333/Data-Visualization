package io.dataease.auth.service.impl;

import io.dataease.auth.api.dto.DynamicMenuDto;
import io.dataease.auth.api.dto.MenuMeta;
import io.dataease.auth.service.DynamicMenuService;
import io.dataease.base.domain.SysMenu;
import io.dataease.base.domain.SysMenuExample;
import io.dataease.base.mapper.SysMenuMapper;
import io.dataease.base.mapper.ext.ExtPluginSysMenuMapper;
import io.dataease.plugins.common.dto.PluginSysMenu;
import io.dataease.plugins.util.PluginUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DynamicMenuServiceImpl implements DynamicMenuService {

    @Autowired(required = false)
    private SysMenuMapper sysMenuMapper;

    @Resource
    private ExtPluginSysMenuMapper extPluginSysMenuMapper;


    /**
     * @Author @Lijian
     * @Description //这个方法就是根据用户获取菜单的具体实现方法
     * @Date 2023/12/7 16:04
     * @return
     **/

    @Override
    public List<DynamicMenuDto> load(String userId) {
        //创建一个查询条件
        SysMenuExample sysMenuExample = new SysMenuExample();
        //查询条件的类型小于等于1
        sysMenuExample.createCriteria().andTypeLessThanOrEqualTo(1);
        //根据菜单排序进行排序
        sysMenuExample.setOrderByClause(" menu_sort ");
        //查询出所有的菜单，到这里就是查询出了所有的通用菜单
        List<SysMenu> sysMenus = sysMenuMapper.selectByExample(sysMenuExample);
        //通过当前类的convert方法，将通用菜单转换成动态菜单
        List<DynamicMenuDto> dynamicMenuDtos = sysMenus.stream().map(this::convert).collect(Collectors.toList());
        //增加插件中的菜单
        List<PluginSysMenu> pluginSysMenus = PluginUtils.pluginMenus();


        //如果插件菜单不为空，
        if (CollectionUtils.isNotEmpty(pluginSysMenus) ) {
            //过滤掉插件菜单中的type大于1的菜单
            pluginSysMenus = pluginSysMenus.stream().filter(menu -> menu.getType() <= 1).collect(Collectors.toList());
            //将插件菜单转换成动态菜单
            List<DynamicMenuDto> pluginDtos = pluginSysMenus.stream().map(this::convert).collect(Collectors.toList());
            //将插件菜单添加到动态菜单中
            dynamicMenuDtos.addAll(pluginDtos);
        }
        //将动态菜单按照菜单排序进行排序
        dynamicMenuDtos = dynamicMenuDtos.stream().sorted((s1, s2) -> {
            int sortIndex1 = null == s1.getMenuSort() ? 999: s1.getMenuSort();
            int sortIndex2 = null == s2.getMenuSort() ? 999: s2.getMenuSort();
            return sortIndex1 - sortIndex2;
        }).collect(Collectors.toList());
        //将动态菜单按照是否隐藏进行排序
        dynamicMenuDtos.sort((s1, s2) -> s1.getHidden().compareTo(s2.getHidden()));
        //将动态菜单构建成树形结构
        List<DynamicMenuDto> result = buildTree(dynamicMenuDtos);
        return result;
    }


    /*
     * @Author @Lijian
     * @Description //这是一个转换方法，将通用菜单转换成动态菜单
     * @Date
     * @return
     **/

    private DynamicMenuDto convert(SysMenu sysMenu){
        DynamicMenuDto dynamicMenuDto = new DynamicMenuDto();
        dynamicMenuDto.setId(sysMenu.getMenuId());
        dynamicMenuDto.setPid(sysMenu.getPid());
        dynamicMenuDto.setName(sysMenu.getName());
        dynamicMenuDto.setPath(sysMenu.getPath());
        dynamicMenuDto.setRedirect(null);
        dynamicMenuDto.setType(sysMenu.getType());
        dynamicMenuDto.setComponent(sysMenu.getComponent());
        MenuMeta menuMeta = new MenuMeta();
        menuMeta.setTitle(sysMenu.getTitle());
        menuMeta.setIcon(sysMenu.getIcon());
        dynamicMenuDto.setMeta(menuMeta);
        dynamicMenuDto.setPermission(sysMenu.getPermission());
        dynamicMenuDto.setMenuSort(sysMenu.getMenuSort());
        dynamicMenuDto.setHidden(sysMenu.getHidden());
        dynamicMenuDto.setIsPlugin(false);
        return dynamicMenuDto;
    }
    //这是一个转换方法，将插件菜单转换成动态菜单
    private DynamicMenuDto convert(PluginSysMenu sysMenu){
        DynamicMenuDto dynamicMenuDto = new DynamicMenuDto();
        dynamicMenuDto.setId(sysMenu.getMenuId());
        dynamicMenuDto.setPid(sysMenu.getPid());
        dynamicMenuDto.setName(sysMenu.getName());
        dynamicMenuDto.setPath(sysMenu.getPath());
        dynamicMenuDto.setRedirect(null);
        dynamicMenuDto.setType(sysMenu.getType());
        dynamicMenuDto.setComponent(sysMenu.getComponent());
        MenuMeta menuMeta = new MenuMeta();
        menuMeta.setTitle(sysMenu.getTitle());
        menuMeta.setIcon(sysMenu.getIcon());
        dynamicMenuDto.setMeta(menuMeta);
        dynamicMenuDto.setPermission(sysMenu.getPermission());
        dynamicMenuDto.setMenuSort(sysMenu.getMenuSort());
        dynamicMenuDto.setHidden(sysMenu.getHidden());
        dynamicMenuDto.setIsPlugin(true);
        dynamicMenuDto.setNoLayout(!!sysMenu.isNoLayout());
        return dynamicMenuDto;
    }


    //这是一个构建树的方法，将动态菜单构建成树形结构
    private List<DynamicMenuDto> buildTree(List<DynamicMenuDto> lists){
        List<DynamicMenuDto> rootNodes = new ArrayList<>();
        lists.forEach(node -> {
            if (isParent(node.getPid())) {
                rootNodes.add(node);
            }
            lists.forEach(tNode -> {
                if (tNode.getPid() == node.getId()) {
                    if (node.getChildren() == null) {
                        node.setChildren(new ArrayList<DynamicMenuDto>());
                        node.setRedirect(node.getPath()+"/"+tNode.getPath());//第一个子节点的path
                    }
                    node.getChildren().add(tNode);
                }
            });
        });
        return rootNodes;

    }

    private Boolean isParent(Long pid){
        return null == pid || pid==0L;
    }

    @Transactional
    public void syncPluginMenu() {
        List<PluginSysMenu> pluginSysMenuList = PluginUtils.pluginMenus();
        extPluginSysMenuMapper.deletePluginMenu();
        if(CollectionUtils.isNotEmpty(pluginSysMenuList)){
            extPluginSysMenuMapper.savePluginMenu(pluginSysMenuList);
        }
    }
}
