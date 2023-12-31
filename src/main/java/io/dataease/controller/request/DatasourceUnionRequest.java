package io.dataease.controller.request;

import io.dataease.base.domain.Datasource;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-05-18
 * Description:
 */
@Data
public class DatasourceUnionRequest  extends Datasource {

    private String userId;

    private String sort;

}
