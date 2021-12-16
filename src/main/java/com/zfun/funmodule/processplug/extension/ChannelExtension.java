package com.zfun.funmodule.processplug.extension;

import com.android.builder.model.SigningConfig;

import java.util.List;

/**
 * {@link  MultiChannelEx}的【配置容器】扩展。<br/>
 * 实现下面这中配置：
 * <pre>
 * <code>
 * productFlavors {
 *         demo {
 *             dimension "mode"
 *         }
 *         full {
 *             dimension "mode"
 *         }
 * }
 * </code>
 * </pre>
 * <br/>
 * 这个类不能写成内部类，静态内部类也不行。
 *
 * <p>
 * Created by zfun on 2021/12/14 10:56 AM
 */
public class ChannelExtension {
    public String name;//这个类必须有个字段为
    public List<String> childFlavors;//一个元素代表一个唯一标识一个渠道的字符串

    ChannelExtension(String name) {
        this.name = name;
    }
}
