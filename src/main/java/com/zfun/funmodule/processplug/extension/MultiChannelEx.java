package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

/**
 * 需要在application的build.gradle中配置。
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
 * <br/>
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
public class MultiChannelEx extends BaseExtension {
    public String prefix;
    public String subfix;

    //
    private final NamedDomainObjectContainer<ChannelExtension> channelConfig;

    // project.getExtensions().create(aPair.getKey(),aPair.getValue(),project);第三个参数传递过来
    public MultiChannelEx(Project project){
        super(project);
        channelConfig = project.container(ChannelExtension.class);
    }

    /**
     * <pre>
     * multiChannel{
     *     ...
     *     //会执行此方法
     *     channelConfig{
     *
     *     }
     *     ...
     * }
     * </pre>
     * */
    //此方法被gradle自动调用
    public void channelConfig(Action<NamedDomainObjectContainer<ChannelExtension>> action) {
        action.execute(channelConfig);
    }

    //自己调用，来获取用户设置的 channelConfig 信息
    public NamedDomainObjectContainer<ChannelExtension> getChannelConfig() {
        return channelConfig;
    }

    @Override
    public boolean isEmpty() {
        return channelConfig.isEmpty();
    }

    @Override
    public String toString() {
        return "MultiChannelEx{" +
                "prefix='" + prefix + '\'' +
                ", subfix='" + subfix + '\'' +
                '}';
    }
}