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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

public class Decorations {
    /**
     * "Persistent" decorations will be copied to handler method calls which targeted the original instruction.
     * This should be used as a prefix to an existing decoration key.
     */
    public static final String PERSISTENT = "mixinextras_persistent_";
    /**
     * Stores that a non-void operation has its result immediately popped and so can be treated as void by someone
     * using {@link WrapWithCondition} on it.
     */
    public static final String POPPED_OPERATION = "mixinextras_operationIsImmediatelyPopped";
    /**
     * Stores a map of LVT index of target local -&gt; LVT index of applicable {@link LocalRef}.
     * References should be shared by injectors that wrap/replace the same target instruction.
     */
    public static final String LOCAL_REF_MAP = "mixinextras_localRefMap";
    /**
     * Stores that a NEW instruction is immediately DUPed so that {@link WrapOperation} can handle it properly.
     */
    public static final String NEW_IS_DUPED = "mixinextras_newIsDuped";
    /**
     * Stores the types that a NEW's &lt;init&gt; takes as its parameters.
     */
    public static final String NEW_ARG_TYPES = "mixinextras_newArgTypes";

    /**
     * Stores that this node has been wrapped by a {@link WrapOperation}.
     */
    public static final String WRAPPED = "mixinextras_wrappedOperation";

    /**
     * Stores the shared CallbackInfo local index for this target instruction.
     */
    public static final String CANCELLABLE_CI_INDEX = "mixinextras_cancellableCiIndex";
}
