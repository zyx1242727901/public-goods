package com.xh.publicgoods.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;

@ContentRowHeight(18)
@HeadRowHeight(25)
@ColumnWidth(25)
@Data
public class DataModel {
    @ExcelProperty("回合")
    private Long round;
    @ExcelProperty("用户名")
    private String userName;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("存入金额")
    private BigDecimal investMoney;
    @ExcelProperty("反应时间/秒")
    private Integer timeScan;
    @ExcelProperty("积攒金额")
    private BigDecimal bounsMoney;
    @ExcelProperty("游戏结束时账户总金额")
    private BigDecimal sumMoney;
    @ExcelProperty("生气")
    private Integer shengqi;
    @ExcelProperty("难过")
    private Integer nanguo;
    @ExcelProperty("敬畏")
    private Integer jingwei;
    @ExcelProperty("厌恶")
    private Integer yanwu;
    @ExcelProperty("开心")
    private Integer kaixin;
    @ExcelProperty("害怕")
    private Integer haipa;
}
