package com.oa.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResultVO {

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private List<ImportError> errors = new ArrayList<>();
}
