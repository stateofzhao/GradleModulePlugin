package com.zfun.funmodule.process.extension;

import java.util.List;
import java.util.Map;

/**
 * 需要在application的build.gradle中配置。
 * {@link  MultiChannelEx}的【配置容器】扩展。<br>
 * 实现下面这中配置：
 * <pre>
 *  productFlavors {
 *         demo {
 *             dimension "mode"
 *         }
 *         Full {
 *             dimension "mode"
 *         }
 *  }
 *  multiChannel{
 *     buildType = 1
 *     channelConfig{
 *         Full{
 *             childFlavors = ["huawei","360"]
 *             checkChannel = false //打包完成后是否读取下最终apk中的渠道信息和写入的渠道信息相等
 *             lowMemory = false //
 *             extraInfo = ["key1":"value1","key2":"value2"] //除渠道外的额外信息
 *         }
 *      }
 *  }
 * </pre>
 * Created by zfun on 2021/12/14 10:56 AM
 */
public class ChannelExtension {
    public String name;//这个类必须有个字段为 name，Gradle会给他赋值，针对上述例子此name就是 Full
    public List<String> childFlavors;//一个元素代表一个唯一标识一个渠道的字符串
    public String subfix;
    public boolean lowMemory;//
    public boolean checkChannel;//打包完成后是否读取下最终apk中的渠道信息和写入的渠道信息相等
    public Map<String,String> extraInfo;//除渠道外的额外信息

    public ChannelExtension(String name) {
        this.name = name;
    }
}
