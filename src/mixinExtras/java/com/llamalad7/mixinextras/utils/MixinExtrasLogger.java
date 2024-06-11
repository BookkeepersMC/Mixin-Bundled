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

import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public interface MixinExtrasLogger {
    void warn(String message, Object... args);

    void info(String message, Object... args);

    void debug(String message, Object... args);

    void error(String message, Throwable t);

    static MixinExtrasLogger get(String name) {
        Object impl;
        try {
            IMixinService service = MixinService.getService();
            Method getLogger = service.getClass().getMethod("getLogger", String.class);
            impl = getLogger.invoke(service, "MixinExtras|" + name);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e1) {
            try {
                impl = Class.forName("org.apache.logging.log4j.LogManager").getMethod("getLogger", String.class).invoke(null, name);
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                     NoSuchMethodException e2) {
                RuntimeException e = new IllegalStateException("Could not get logger! Please inform LlamaLad7!");
                e.addSuppressed(e1);
                e.addSuppressed(e2);
                throw e;
            }
        }
        Object finalImpl = impl;
        return (MixinExtrasLogger) Proxy.newProxyInstance(MixinExtrasLogger.class.getClassLoader(), new Class[]{MixinExtrasLogger.class}, (proxy, method, args) -> {
            return finalImpl.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(finalImpl, args);
        });
    }
}
