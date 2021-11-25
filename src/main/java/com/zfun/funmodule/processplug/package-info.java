/**
 * 实现逻辑：<br/>
 * 一个Project可以设置多个扩展{@link com.zfun.funmodule.BaseExtension}；<br/>
 * 每个扩展对应一个{@link com.zfun.funmodule.processplug.IProcess}；<br/>
 * 最终就是一个Project可以由多个{@link com.zfun.funmodule.processplug.IProcess}来处理。<br/>
 * <p/>
 * 各个Project都可以单独设置自己的扩展，如果不设置的话会使用根Project的扩展。
 * <p/>
 * 目前还没有处理各个{@link com.zfun.funmodule.processplug.IProcess}之间如何交互，所以各个
 * {@link com.zfun.funmodule.processplug.IProcess}都是独立完成自己的工作。
 * <p>
 * Created by zfun on 2021/11/25 2:28 下午
 */
package com.zfun.funmodule.processplug;