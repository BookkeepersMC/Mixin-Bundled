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
package com.llamalad7.mixinextras.versions;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.util.VersionNumber;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public abstract class MixinVersion {
    private static final List<String> VERSIONS = Arrays.asList("0.8.4", "0.8.3", "0.8");
    private static final MixinVersion INSTANCE;

    static {
        VersionNumber currentVersion = VersionNumber.parse(MixinEnvironment.getCurrentEnvironment().getVersion());
        MixinVersion current = null;
        for (String version : VERSIONS) {
            if (VersionNumber.parse(version).compareTo(currentVersion) > 0) {
                continue;
            }
            try {
                Class<?> implClass = Class.forName(
                        MixinVersion.class.getPackage().getName() + ".MixinVersionImpl" + "_v" + version.replace('.', '_')
                );
                current = (MixinVersion) implClass.getConstructor().newInstance();
                break;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        INSTANCE = current;
    }

    public static MixinVersion getInstance() {
        return INSTANCE;
    }

    public abstract RuntimeException makeInvalidInjectionException(InjectionInfo info, String message);

    public abstract IMixinContext getMixin(InjectionInfo info);

    public abstract LocalVariableDiscriminator.Context makeLvtContext(InjectionInfo info, Type returnType, boolean argsOnly, Target target, AbstractInsnNode node);

    public abstract void preInject(InjectionInfo info);

    public abstract AnnotationNode getAnnotation(InjectionInfo info);
}
