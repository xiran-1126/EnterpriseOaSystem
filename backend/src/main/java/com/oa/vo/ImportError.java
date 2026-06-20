package com.oa.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {

    private Integer rowNum;

    private String errorMsg;

    private UserImportVO rowData;
}
