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
package com.llamalad7.mixinextras.sugar.impl;

import com.llamalad7.mixinextras.injector.StackExtension;
import com.llamalad7.mixinextras.service.MixinExtrasService;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.utils.ASMUtils;
import com.llamalad7.mixinextras.utils.CompatibilityHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class SugarApplicator {
    private static final Map<String, Class<? extends SugarApplicator>> MAP = new HashMap<>();

    static {
        List<Pair<Class<? extends Annotation>, Class<? extends SugarApplicator>>> sugars = Arrays.asList(
                Pair.of(Cancellable.class, CancellableSugarApplicator.class),
                Pair.of(Local.class, LocalSugarApplicator.class),
                Pair.of(Share.class, ShareSugarApplicator.class)
        );
        for (Pair<Class<? extends Annotation>, Class<? extends SugarApplicator>> pair : sugars) {
            for (String name : MixinExtrasService.getInstance().getAllClassNames(pair.getLeft().getName())) {
                MAP.put('L' + name.replace('.', '/') + ';', pair.getRight());
            }
        }
    }

    protected final IMixinInfo mixin;
    protected final InjectionInfo info;
    protected final AnnotationNode sugar;
    protected final Type paramType;
    protected final Type paramGeneric;
    protected final int paramLvtIndex;
    protected final int paramIndex;

    SugarApplicator(InjectionInfo info, SugarParameter parameter) {
        this.mixin = CompatibilityHelper.getMixin(info).getMixin();
        this.info = info;
        this.sugar = parameter.sugar;
        this.paramType = parameter.type;
        this.paramGeneric = parameter.genericType;
        this.paramLvtIndex = parameter.lvtIndex;
        this.paramIndex = parameter.paramIndex;
    }

    abstract void validate(Target target, InjectionNode node);

    abstract void prepare(Target target, InjectionNode node);

    abstract void inject(Target target, InjectionNode node, StackExtension stack);

    int postProcessingPriority() {
        throw new UnsupportedOperationException(
                String.format(
                        "Sugar type %s does not support post-processing! Please inform LlamaLad7!",
                        ASMUtils.annotationToString(sugar)
                )
        );
    }

    static SugarApplicator create(InjectionInfo info, SugarParameter parameter) {
        try {
            Class<? extends SugarApplicator> clazz = MAP.get(parameter.sugar.desc);
            Constructor<? extends SugarApplicator> ctor = clazz.getDeclaredConstructor(InjectionInfo.class, SugarParameter.class);
            return ctor.newInstance(info, parameter);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean isSugar(String desc) {
        return MAP.containsKey(desc);
    }
}
