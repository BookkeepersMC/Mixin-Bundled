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

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
interface InternalField<O, T> {
    T get(O owner);

    void set(O owner, T newValue);

    static <O, T> InternalField<O, T> of(Class<?> clazz, String name) {
        Field impl;
        try {
            impl = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    String.format(
                            "Failed to find field %s::%s! Please report to LlamaLad7!",
                            clazz, name
                    ), e
            );
        }
        impl.setAccessible(true);
        return new InternalField<O, T>() {
            @Override
            public T get(O owner) {
                try {
                    return (T) impl.get(owner);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            String.format(
                                    "Failed to get %s::%s on %s! Please report to LlamaLad7!",
                                    clazz, name, owner
                            ), e
                    );
                }
            }

            @Override
            public void set(O owner, T newValue) {
                try {
                    impl.set(owner, newValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            String.format(
                                    "Failed to set %s::%s to %s on %s! Please report to LlamaLad7!",
                                    clazz, name, newValue, owner
                            ), e
                    );
                }
            }
        };
    }

    static <O, T> InternalField<O, T> of(String clazz, String name) {
        try {
            return of(Class.forName(clazz), name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Failed to find class %s! Please report to LlamaLad7!", clazz), e);
        }
    }
}
