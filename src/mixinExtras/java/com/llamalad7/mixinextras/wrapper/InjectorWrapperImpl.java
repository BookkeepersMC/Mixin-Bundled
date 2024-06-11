/*
 * This file is part of Mixin, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.llamalad7.mixinextras.wrapper;

import com.llamalad7.mixinextras.sugar.impl.SingleIterationList;
import com.llamalad7.mixinextras.utils.CompatibilityHelper;
import com.llamalad7.mixinextras.utils.MixinInternals;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

import java.util.*;

public abstract class InjectorWrapperImpl {
    @FunctionalInterface
    public interface Factory {

        InjectorWrapperImpl create(InjectionInfo wrapper, MixinTargetContext mixin, MethodNode method, AnnotationNode annotation);
    }

    @FunctionalInterface
    public interface HandlerCallCallback {
        void onFound(Target target, InjectionNode sourceNode, MethodInsnNode call);
    }

    private final InjectionInfo wrapperInfo;
    protected final ClassNode classNode;

    private final boolean useGranularInject;

    protected InjectorWrapperImpl(InjectionInfo wrapper, MixinTargetContext mixin, MethodNode method, AnnotationNode annotation, boolean useGranularInject) {
        this.wrapperInfo = wrapper;
        this.classNode = mixin.getTargetClassNode();
        this.useGranularInject = useGranularInject;
    }

    public boolean usesGranularInject() {
        return useGranularInject;
    }

    protected abstract InjectionInfo getDelegate();

    protected abstract MethodNode getHandler();

    protected boolean isValid() {
        return getDelegate().isValid();
    }

    protected void prepare() {
        getDelegate().prepare();
        MethodNode handler = getHandler();
        handler.visibleAnnotations.remove(InjectionInfo.getInjectorAnnotation(CompatibilityHelper.getMixin(wrapperInfo).getMixin(), handler));
    }

    protected void preInject() {
        CompatibilityHelper.preInject(getDelegate());
    }

    protected void inject() {
        if (useGranularInject) {
            granularInject((target, node, call) -> {});
            return;
        }
        getDelegate().inject();
    }

    protected void granularInject(HandlerCallCallback callback) {
        InjectionInfo delegate = getDelegate();
        if (delegate instanceof WrapperInjectionInfo) {
            WrapperInjectionInfo wrapper = (WrapperInjectionInfo) delegate;
            wrapper.impl.granularInject(callback);
            return;
        }
        doGranularInject(callback);
    }

    protected void doPostInject(Runnable postInject) {
        postInject.run();
    }

    protected void addCallbackInvocation(MethodNode handler) {
        getDelegate().addCallbackInvocation(handler);
    }

    protected RuntimeException granularInjectNotSupported() {
        return new IllegalStateException(
                getDelegate().getClass() + " does not support granular injection! Please report to LlamaLad7!"
        );
    }

    private void doGranularInject(HandlerCallCallback callback) {
        InjectionInfo delegate = getDelegate();
        Map<Target, List<InjectionNode>> targets = MixinInternals.getTargets(delegate);
        Injector injector = MixinInternals.getInjector(delegate);
        for (Map.Entry<Target, List<InjectionNode>> entry : targets.entrySet()) {
            Target target = entry.getKey();
            Set<MethodInsnNode> discoveredHandlerCalls = new HashSet<>(findHandlerCalls(target));
            for (InjectionNode node : entry.getValue()) {
                inject(injector, target, node);
                for (MethodInsnNode handlerCall : findHandlerCalls(target)) {
                    if (discoveredHandlerCalls.add(handlerCall)) {
                        callback.onFound(target, node, handlerCall);
                    }
                }
            }
            postInject(injector, target, entry.getValue());
        }
        targets.clear();
    }

    private List<MethodInsnNode> findHandlerCalls(Target target) {
        MethodNode handler = getHandler();
        List<MethodInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode insn : target) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) insn;
                if (call.owner.equals(classNode.name) && call.name.equals(handler.name) && call.desc.equals(handler.desc)) {
                    result.add(call);
                }
            }
        }
        return result;
    }

    private static void inject(Injector injector, Target target, InjectionNode node) {
        injector.inject(target, new SingleIterationList<>(Collections.singletonList(node), 0));
    }

    private static void postInject(Injector injector, Target target, List<InjectionNode> nodes) {
        injector.inject(target, new SingleIterationList<>(nodes, 1));
    }
}
