package com.zfun.funmodule.processplug.extension;

import java.util.List;
import java.util.Map;

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
 * <br/>
 * build.gradle -
 * <pre>
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
 *   }
 * </pre>
 * <p>
 * Created by zfun on 2021/12/14 10:56 AM
 */
public class ChannelExtension {
    public String name;//这个类必须有个字段为
    public List<String> childFlavors;//一个元素代表一个唯一标识一个渠道的字符串
    public boolean lowMemory;//
    public boolean checkChannel;//打包完成后是否读取下最终apk中的渠道信息和写入的渠道信息相等
    public Map<String,String> extraInfo;//除渠道外的额外信息

    public ChannelExtension(String name) {
        this.name = name;
    }
}
