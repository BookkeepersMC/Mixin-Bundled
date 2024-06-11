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
package com.llamalad7.mixinextras.sugar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows you to share values between handler methods in the same target method. This is generally preferable to storing
 * the value in a field because this is thread-safe and does not consume any long-term memory.
 * <p>
 * You must provide an ID for the shared value and the annotated parameter's type must be one of the
 * {@link com.llamalad7.mixinextras.sugar.ref.LocalRef LocalRef}
 * family.
 * The parameter's name is irrelevant and only the ID in the annotation is used to share matching values.
 * <p>
 * The same reference objects will be passed to all handler methods requesting a given ID within a given target method
 * invocation. By default, IDs are per-mixin, so you don't need to worry about including your modid or anything similar
 * in them. If you specifically want to share values across mixins, set the {@link Share#namespace()}.
 * <p>
 * Note: If a {@code @Share}d value is read from before it has been written to, no exception is thrown and it will simply return
 * the default value for that type (0, 0f, null, etc), much like a field.
 * <p>
 * See <a href="https://github.com/LlamaLad7/MixinExtras/wiki/Share">the wiki article</a> for more info.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface Share {
    /**
     * The id for this shared value.
     */
    String value();

    /**
     * The namespace for this shared value. By default, this is the fully-qualified name of the enclosing mixin class
     * to ensure uniqueness. If you want to share values between mixin classes, you can set this to something else,
     * e.g. your mod ID.
     */
    String namespace() default "";
}
