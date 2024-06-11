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
package com.llamalad7.mixinextras.utils;

import com.llamalad7.mixinextras.versions.MixinVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator.Context;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.refmap.IMixinContext;

/**
 * Helpers for writing code that is compatible with all variants of Mixin 0.8+
 */
public class CompatibilityHelper {
    public static RuntimeException makeInvalidInjectionException(InjectionInfo info, String message) {
        return MixinVersion.getInstance().makeInvalidInjectionException(info, message);
    }

    public static IMixinContext getMixin(InjectionInfo info) {
        return MixinVersion.getInstance().getMixin(info);
    }

    public static Context makeLvtContext(InjectionInfo info, Type returnType, boolean argsOnly, Target target, AbstractInsnNode node) {
        return MixinVersion.getInstance().makeLvtContext(info, returnType, argsOnly, target, node);
    }

    public static void preInject(InjectionInfo info) {
        MixinVersion.getInstance().preInject(info);
    }

    public static AnnotationNode getAnnotation(InjectionInfo info) {
        return MixinVersion.getInstance().getAnnotation(info);
    }
}
