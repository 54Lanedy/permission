package com.mmall.dto;

import com.google.common.collect.Lists;
import com.mmall.model.SysDept;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * Created by liyue
 * Time 2019/9/21 10:58
 */
@Getter
@Setter
@ToString
public class DeptLevelDto extends SysDept {

    private List<DeptLevelDto> deptList = Lists.newArrayList();

    public static DeptLevelDto adept(SysDept dept){
        DeptLevelDto dto = new DeptLevelDto();
        BeanUtils.copyProperties(dept,dto);
        return dto;
    }
}
