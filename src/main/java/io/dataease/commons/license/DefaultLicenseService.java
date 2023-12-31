package io.dataease.commons.license;

import com.google.gson.Gson;
import io.dataease.base.domain.License;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


//这个类就是用来获取本地系统中的默认F2CLicenseResponse许可证的类
@Service
public class DefaultLicenseService {
    @Resource
    private InnerLicenseService innerLicenseService;
    @Value("${spring.application.name:null}")
    private String moduleId;

    private static final String LICENSE_ID = "fit2cloud_license";
    private static final String validatorUtil = "/usr/bin/validator";
    private static final String product = "DataEase";
    /*private static final String[] NO_PLU_LIMIT_MODULES = new String[]{"dashboard", "gateway"};*/



    //获取F2CLicenseResponse许可证的方法
    //就是从本地系统文件中获取到licence的值和参数中license值相同的F2CLicenseResponse许可证
    public F2CLicenseResponse validateLicense(String product, String licenseKey){
        List<String> command = new ArrayList<String>();
        StringBuilder result = new StringBuilder();
        //将系统路径添加到command中
        command.add(validatorUtil);
        //将从数据库中年获取到的license添加到command中
        command.add(licenseKey);
        try{
            execCommand(result, command);
            LogUtil.info("read lic content is : " + result.toString());
            //Gson是google提供的一个用来处理json的工具类，这里是将result转换成F2CLicenseResponse
            F2CLicenseResponse f2CLicenseResponse = new Gson().fromJson(result.toString(), F2CLicenseResponse.class);
            //如果F2CLicenseResponse的状态不是有效的，就直接返回F2CLicenseResponse
            if(f2CLicenseResponse.getStatus() != F2CLicenseResponse.Status.valid){
                return f2CLicenseResponse;
            }
            //如果F2ClicenseResponse的许可正是有效的，就判断许可证的产品是否和当前的产品相同，
            //如果不相同，就将F2CLicenseResponse的状态设置为无效，将F2CLicenseResponse的许可证设置为空，
            // 将F2CLicenseResponse的信息设置为The license is unavailable for this product.
            if(!StringUtils.equals(f2CLicenseResponse.getLicense().getProduct(), product)){
                f2CLicenseResponse.setStatus(F2CLicenseResponse.Status.invalid);
                f2CLicenseResponse.setLicense(null);
                f2CLicenseResponse.setMessage("The license is unavailable for this product.");
                return f2CLicenseResponse;
            }
            return f2CLicenseResponse;
        }catch (Exception e){
            LogUtil.error(e.getMessage());
            // e.printStackTrace();
            // return F2CLicenseResponse.invalid(e.getMessage());
            return F2CLicenseResponse.noRecord();
        }

    }


    //执行系统命令或者是外部命令的方法
    private static int execCommand(StringBuilder result, List<String> command) throws Exception{
        //创建一个进程构造器
        ProcessBuilder builder = new ProcessBuilder();
        //设置进程的命令
        builder.command(command);
        //启动进程
        Process process = builder.start();
        //获取进程的输入流
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line=bufferedReader.readLine()) != null){
            //将进程的输入流的内容添加到result中
            result.append(line).append("\n");
        }
        //获取进程的退出值
        int exitCode = process.waitFor();
        command.clear();
        return exitCode;
    }


    //获取F2CLicenseResponse许可证的方法，本质上是调用了validateLicense的有参方法
    public F2CLicenseResponse validateLicense() {
        try {
            //先获取默认的license
            License license  = readLicense();
            //然后将license转换成F2CLicenseResponse
            return validateLicense(product, license.getLicense());
        } catch (Exception e) {
            return F2CLicenseResponse.noRecord();
        }
    }

    public F2CLicenseResponse updateLicense(String product, String licenseKey) {
        // 验证license
        F2CLicenseResponse response = validateLicense(product, licenseKey);
        if (response.getStatus() != F2CLicenseResponse.Status.valid) {
            return response;
        }
        // 覆盖原license
        writeLicense(licenseKey, response);
        return response;
    }

    // 从数据库读取License对象
    public License readLicense() {
        //先通过默认的id从数据库的license表中获取license对象
        License license = innerLicenseService.getLicense(LICENSE_ID);
        //如果license为空，就抛出异常
        if (license == null) {
            DEException.throwException("i18n_no_license_record");
        }
        //如果license的license为空，就抛出异常
        //license实体类中有几个许可证，licence是其中一个
        if (StringUtils.isBlank(license.getLicense())) {
            DEException.throwException("i18n_license_is_empty");
        }
        return license;
    }

    // 创建或更新License
    private void writeLicense(String licenseKey, F2CLicenseResponse response) {
        if (StringUtils.isBlank(licenseKey)) {
            DEException.throwException("i18n_license_is_empty");
        }
        License license = new License();
        license.setId(LICENSE_ID);
        license.setLicense(licenseKey);
        license.setF2cLicense(new Gson().toJson(response));
        innerLicenseService.saveLicense(license);
    }
}
