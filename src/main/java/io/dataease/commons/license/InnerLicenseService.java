package io.dataease.commons.license;

import io.dataease.base.domain.License;
import io.dataease.base.mapper.LicenseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/*
 * @Author @Lijian
 * @Description //这个类主要是用来处理许可证的一些操作的
 * @Date
 * @return
 **/

@Service
//这个注解是用来标记这个类中的所有方法都需要被事务管理，rollbackFor属性指定回滚的异常，这里是Exception，
// 也就是说只要方法中抛出了Exception异常，就会回滚
@Transactional(rollbackFor = Exception.class)
class InnerLicenseService {

    @Resource
    private LicenseMapper licenseMapper;

    //判断是否存在许可证
    boolean existLicense(String key) {
        License license = licenseMapper.selectByPrimaryKey(key);
        return license != null;
    }

    //获取许可证
    License getLicense(String key) {
        License license = licenseMapper.selectByPrimaryKey(key);
        if (license == null) return null;
        return license;
    }

    //保存许可证
    void saveLicense(License license) {
        license.setUpdateTime(new Date());
        if (existLicense(license.getId())) {
            licenseMapper.updateByPrimaryKey(license);
        } else {
            licenseMapper.insert(license);
        }
    }


}