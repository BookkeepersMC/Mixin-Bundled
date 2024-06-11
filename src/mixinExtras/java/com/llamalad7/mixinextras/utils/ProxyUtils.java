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

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyUtils {
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Object impl, Class<T> interfaceClass) {
        if (interfaceClass.isInstance(impl)) {
            return interfaceClass.cast(impl);
        }
        String simpleName = interfaceClass.getSimpleName();
        if (ClassUtils.getAllInterfaces(impl.getClass()).stream().anyMatch(it -> it.getName().endsWith('.' + simpleName))) {
            return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
                Method original = impl.getClass().getMethod(method.getName(), method.getParameterTypes());
                original.setAccessible(true);
                return original.invoke(impl, args);
            });
        }
        throw new UnsupportedOperationException(String.format("Cannot get a %s instance from %s", simpleName, impl));
    }
}
