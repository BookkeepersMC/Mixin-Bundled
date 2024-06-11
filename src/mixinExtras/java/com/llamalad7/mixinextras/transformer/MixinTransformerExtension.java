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
package com.llamalad7.mixinextras.transformer;

import com.llamalad7.mixinextras.sugar.impl.SugarMixinTransformer;
import com.llamalad7.mixinextras.utils.MixinInternals;
import com.llamalad7.mixinextras.wrapper.factory.FactoryRedirectWrapperMixinTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.*;

public class MixinTransformerExtension implements IExtension {
    private final Set<ClassNode> preparedMixins = Collections.newSetFromMap(new WeakHashMap<>());
    private final List<MixinTransformer> transformers = Arrays.asList(
            new FactoryRedirectWrapperMixinTransformer(), new SugarMixinTransformer()
    );

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) {
        for (Pair<IMixinInfo, ClassNode> pair : MixinInternals.getMixinsFor(context)) {
            IMixinInfo info = pair.getLeft();
            ClassNode node = pair.getRight();
            if (preparedMixins.contains(node)) {
                // Don't scan the whole class again
                continue;
            }
            for (MixinTransformer transformer : transformers) {
                transformer.transform(info, node);
            }
            preparedMixins.add(node);
        }
    }

    @Override
    public void postApply(ITargetClassContext context) {
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
    }
}
