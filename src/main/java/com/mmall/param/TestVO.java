package com.mmall.param;

import com.mmall.model.SysAcl;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by liyue
 * Time 2019/9/16 23:19
 */
@Getter
@Setter
public class TestVO {
    @NotNull
    private Integer id;

    @NotBlank
    private String msg;

//    @NotEmpty
    private List<SysAcl> src;
}
