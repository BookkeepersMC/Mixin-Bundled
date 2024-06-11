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
package com.llamalad7.mixinextras.service;

import com.llamalad7.mixinextras.utils.Blackboard;
import com.llamalad7.mixinextras.utils.ProxyUtils;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;

public interface MixinExtrasService {
    int getVersion();

    boolean shouldReplace(Object otherService);

    void takeControlFrom(Object olderService);

    void concedeTo(Object newerService, boolean wasActive);

    void offerPackage(int version, String packageName);

    void offerExtension(int version, IExtension extension);

    void offerInjector(int version, Class<? extends InjectionInfo> injector);

    void initialize();

    static void setup() {
        Object latestImpl = Blackboard.get("MixinExtrasServiceInstance");
        if (latestImpl == null) {
            MixinExtrasService newImpl = new MixinExtrasServiceImpl();
            Blackboard.put("MixinExtrasServiceInstance", newImpl);
            newImpl.takeControlFrom(null);
            return;
        }
        MixinExtrasService ourImpl = new MixinExtrasServiceImpl();
        if (ourImpl.shouldReplace(latestImpl)) {
            getFrom(latestImpl).concedeTo(ourImpl, true);
            Blackboard.put("MixinExtrasServiceInstance", ourImpl);
            ourImpl.takeControlFrom(latestImpl);
        } else {
            ourImpl.concedeTo(latestImpl, false);
        }
    }

    static MixinExtrasService getFrom(Object serviceImpl) {
        return ProxyUtils.getProxy(serviceImpl, MixinExtrasService.class);
    }

    static MixinExtrasServiceImpl getInstance() {
        Object impl = Blackboard.get("MixinExtrasServiceInstance");
        if (impl instanceof MixinExtrasServiceImpl) {
            MixinExtrasServiceImpl ourImpl = (MixinExtrasServiceImpl) impl;
            if (ourImpl.initialized) {
                return ourImpl;
            }
            throw new IllegalStateException("Cannot use service because it is not initialized!");
        }
        throw new IllegalStateException("Cannot use service because another service is active: " + impl);
    }
}
