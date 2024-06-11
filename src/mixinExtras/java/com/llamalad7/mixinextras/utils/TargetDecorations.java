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

import org.spongepowered.asm.mixin.injection.struct.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class TargetDecorations {
    private static final Map<Target, Map<String, Object>> IMPL = new WeakHashMap<>();

    public static boolean has(Target target, String key) {
        return IMPL.containsKey(target) && IMPL.get(target).containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Target target, String key) {
        return IMPL.containsKey(target) ? (T) IMPL.get(target).get(key) : null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOrPut(Target target, String key, Supplier<T> supplier) {
        return (T) IMPL.computeIfAbsent(target, k -> new HashMap<>()).computeIfAbsent(key, k -> supplier.get());
    }

    public static void put(Target target, String key, Object value) {
        IMPL.computeIfAbsent(target, k -> new HashMap<>()).put(key, value);
    }

    public static <T> void modify(Target target, String key, UnaryOperator<T> operator) {
        IMPL.computeIfAbsent(target, k -> new HashMap<>()).put(key, operator.apply(get(target, key)));
    }

    public static void remove(Target target, String key) {
        if (IMPL.containsKey(target)) {
            IMPL.get(target).remove(key);
        }
    }
}
