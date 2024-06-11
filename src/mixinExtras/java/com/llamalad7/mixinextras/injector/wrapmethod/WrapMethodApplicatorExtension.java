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
package com.llamalad7.mixinextras.injector.wrapmethod;

import com.llamalad7.mixinextras.sugar.impl.ShareInfo;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.*;

public class WrapMethodApplicatorExtension implements IExtension {
    private static final Map<ClassNode, Map<MethodNode, WrapMethodStage>> wrappers = new HashMap<>();

    static void offerWrapper(Target target, MethodNode handler, Type operationType, List<ShareInfo> shares) {
        Map<MethodNode, WrapMethodStage> relevant = wrappers.computeIfAbsent(target.classNode, k -> new LinkedHashMap<>());
        WrapMethodStage inner = relevant.computeIfAbsent(target.method, WrapMethodStage.Vanilla::new);
        relevant.put(target.method, new WrapMethodStage.Wrapper(inner, handler, operationType, shares));
    }

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) {
    }

    @Override
    public void postApply(ITargetClassContext context) {
        ClassNode targetClass = context.getClassNode();
        Map<MethodNode, WrapMethodStage> relevant = wrappers.get(targetClass);
        if (relevant == null) {
            return;
        }
        for (WrapMethodStage wrapper : relevant.values()) {
            wrapper.apply(targetClass, new LinkedHashSet<>());
        }

        wrappers.remove(targetClass);
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
    }
}
