package com.xh.publicgoods.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MoodDTO {
    private int shengqi;
    private int nanguo;
    private int jingwei;
    private int yanwu;
    private int kaixin;
    private int haipa;
}
