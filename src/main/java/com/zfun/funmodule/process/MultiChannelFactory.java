package com.zfun.funmodule.process;

import com.zfun.funmodule.process.extension.MultiChannelEx;
import com.zfun.funmodule.process.process.MultiChannelProcess;
import org.gradle.api.Project;

import java.util.Collections;
import java.util.List;

/**
 * Created by zfun on 2021/12/10 4:38 PM
 */
public class MultiChannelFactory implements IProcessFactory<MultiChannelEx> {
    @Override
    public List<IProcess> createProcess(Project project, MultiChannelEx extension) {
        if(null == extension){
            return Collections.emptyList();
        }
        return Collections.singletonList(new MultiChannelProcess());
    }
}
