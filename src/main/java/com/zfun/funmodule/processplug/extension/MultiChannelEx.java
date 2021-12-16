package com.zfun.funmodule.processplug.extension;

import com.zfun.funmodule.BaseExtension;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

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