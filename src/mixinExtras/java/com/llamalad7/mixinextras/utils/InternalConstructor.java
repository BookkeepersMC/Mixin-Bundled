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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
interface InternalConstructor<T> {
    T newInstance(Object... args);

    static <T> InternalConstructor<T> of(Class<?> clazz, Class<?>... argTypes) {
        Constructor<T> impl;
        try {
            impl = (Constructor<T>) clazz.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    String.format(
                            "Failed to find constructor %s(%s)! Please report to LlamaLad7!",
                            clazz,
                            Arrays.stream(argTypes).map(Class::getName).collect(Collectors.joining(", "))
                    ), e
            );
        }
        impl.setAccessible(true);
        return args -> {
            try {
                return (T) impl.newInstance(args);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(
                        String.format(
                                "Failed to construct %s(%s) with args [%s]! Please report to LlamaLad7!",
                                clazz,
                                Arrays.stream(argTypes).map(Class::getName).collect(Collectors.joining(", ")),
                                Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", "))
                        ), e
                );
            }
        };
    }

    static <T> InternalConstructor<T> of(String clazz, Class<?>... argTypes) {
        try {
            return of(Class.forName(clazz), argTypes);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Failed to find class %s! Please report to LlamaLad7!", clazz), e);
        }
    }
}
