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
package com.llamalad7.mixinextras.sugar.impl.handlers;

import com.llamalad7.mixinextras.service.MixinExtrasService;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.impl.SugarParameter;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler transformers belong to an individual sugar parameter and can transform {@link HandlerInfo}s.
 */
public abstract class HandlerTransformer {
    private static final Map<String, Class<? extends HandlerTransformer>> MAP = new HashMap<>();

    static {
        List<Pair<Class<? extends Annotation>, Class<? extends HandlerTransformer>>> sugars = Arrays.asList(
                Pair.of(Local.class, LocalHandlerTransformer.class)
        );
        for (Pair<Class<? extends Annotation>, Class<? extends HandlerTransformer>> pair : sugars) {
            for (String name : MixinExtrasService.getInstance().getAllClassNames(pair.getLeft().getName())) {
                MAP.put('L' + name.replace('.', '/') + ';', pair.getRight());
            }
        }
    }

    protected final IMixinInfo mixin;
    protected final SugarParameter parameter;

    HandlerTransformer(IMixinInfo mixin, SugarParameter parameter) {
        this.mixin = mixin;
        this.parameter = parameter;
    }

    /**
     * Determines whether this transformer needs to make any changes to the given handler method.
     * If none of the transformers are required, the handler transformation process can be skipped entirely.
     * @param handler the handler method
     * @return whether this transformer needs to make changes to the handler
     */
    public abstract boolean isRequired(MethodNode handler);

    /**
     * Describes the required changes to the handler method.
     * This is only called if the transformer returned true in {@link HandlerTransformer#isRequired}.
     * @param info the handler to be transformed
     */
    public abstract void transform(HandlerInfo info);

    public static HandlerTransformer create(IMixinInfo mixin, SugarParameter parameter) {
        try {
            Class<? extends HandlerTransformer> clazz = MAP.get(parameter.sugar.desc);
            if (clazz == null) {
                return null;
            }
            Constructor<? extends HandlerTransformer> ctor = clazz.getDeclaredConstructor(IMixinInfo.class, SugarParameter.class);
            return ctor.newInstance(mixin, parameter);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
