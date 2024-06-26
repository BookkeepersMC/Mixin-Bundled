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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClassGenUtils {
    private static final Definer DEFINER;
    private static final Map<String, byte[]> DEFINITIONS = new HashMap<>();

    static {
        Definer theDefiner;
        try {
            Method defineClass = Unsafe.class.getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);
            theDefiner = (name, bytes, scope) -> {
                defineClass.invoke(unsafe, name, bytes, 0, bytes.length, scope.lookupClass().getClassLoader(), scope.lookupClass().getProtectionDomain());
            };
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException e1) {
            try {
                Method defineClass = MethodHandles.Lookup.class.getMethod("defineClass", byte[].class);
                theDefiner = (name, bytes, scope) -> {
                    //noinspection PrimitiveArrayArgumentToVarargsMethod
                    defineClass.invoke(scope, bytes);
                };
            } catch (NoSuchMethodException e2) {
                RuntimeException e = new RuntimeException("Could not resolve class definer! Please report to LlamaLad7.");
                e.addSuppressed(e1);
                e.addSuppressed(e2);
                throw e;
            }
        }
        DEFINER = theDefiner;
    }

    public static void defineClass(ClassNode node, MethodHandles.Lookup scope) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        byte[] bytes = writer.toByteArray();
        String name = node.name.replace('/', '.');
        try {
            DEFINER.define(name, bytes, scope);
        } catch (Throwable e) {
            throw new RuntimeException(
                    String.format(
                            "Failed to define class %s from %s! Please report to LlamaLad7!",
                            node.name, scope
                    ), e
            );
        }
        DEFINITIONS.put(name, bytes);
        MixinInternals.registerClassInfo(node);
        MixinInternals.getExtensions().export(MixinEnvironment.getCurrentEnvironment(), node.name, false, node);
    }

    /**
     * Exposed for use in specific custom classloader setups. You probably don't need this.
     */
    public static Map<String, byte[]> getDefinitions() {
        return Collections.unmodifiableMap(DEFINITIONS);
    }

    @FunctionalInterface
    private interface Definer {
        void define(String name, byte[] bytes, MethodHandles.Lookup scope) throws Throwable;
    }
}
