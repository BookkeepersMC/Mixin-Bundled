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
package com.llamalad7.mixinextras.sugar.impl;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.*;

public class SugarPostProcessingExtension implements IExtension {
    private static final Map<String, List<Task>> POST_PROCESSING_TASKS = new HashMap<>();

    static void enqueuePostProcessing(SugarApplicator applicator, Runnable task) {
        POST_PROCESSING_TASKS.computeIfAbsent(applicator.info.getClassNode().name, k -> new ArrayList<>())
                .add(new Task(applicator.postProcessingPriority(), task));
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
        String targetName = context.getClassNode().name;
        List<Task> tasks = POST_PROCESSING_TASKS.remove(targetName);
        if (tasks != null) {
            Collections.sort(tasks);
            tasks.forEach(Task::run);
        }
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
    }

    private static class Task implements Comparable<Task> {
        private final int priority;
        private final Runnable body;

        public Task(int priority, Runnable body) {
            this.priority = priority;
            this.body = body;
        }

        public void run() {
            body.run();
        }

        @Override
        public int compareTo(Task o) {
            return Integer.compare(priority, o.priority);
        }
    }
}
