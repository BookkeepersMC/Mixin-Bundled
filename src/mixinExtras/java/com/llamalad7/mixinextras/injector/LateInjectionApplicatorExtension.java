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
package com.llamalad7.mixinextras.injector;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.*;

/**
 * This extension is responsible for actually injecting all late-applying injectors which were queued up during the
 * normal injection phase. Applying them here means we are guaranteed to run after every other injector, which is
 * crucial.
 */
public class LateInjectionApplicatorExtension implements IExtension {
    private static final Map<ITargetClassContext, Map<String, List<Runnable[]>>> QUEUED_INJECTIONS = Collections.synchronizedMap(new HashMap<>());

    static void offerInjection(ITargetClassContext targetClassContext, LateApplyingInjectorInfo injectorInfo) {
        Map<String, List<Runnable[]>> map = QUEUED_INJECTIONS.computeIfAbsent(targetClassContext, k -> initializeMap());
        map.get(injectorInfo.getLateInjectionType()).add(new Runnable[]{injectorInfo::lateInject, injectorInfo::latePostInject});
    }

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) {
    }

    @Override
    public void postApply(ITargetClassContext context) {
        Map<String, List<Runnable[]>> relevant = QUEUED_INJECTIONS.get(context);
        if (relevant == null) {
            return;
        }
        for (List<Runnable[]> queuedInjections : relevant.values()) {
            for (Runnable[] injection : queuedInjections) {
                injection[0].run();
            }
            for (Runnable[] injection : queuedInjections) {
                injection[1].run();
            }
        }
        QUEUED_INJECTIONS.remove(context);
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
    }

    private static Map<String, List<Runnable[]>> initializeMap() {
        Map<String, List<Runnable[]>> result = new LinkedHashMap<>();
        result.put("WrapWithCondition", new ArrayList<>());
        result.put("WrapOperation", new ArrayList<>());
        return result;
    }
}
