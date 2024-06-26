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
package com.llamalad7.mixinextras.injector.v2;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows you to wrap a
 * {@link org.spongepowered.asm.mixin.injection.points.BeforeInvoke void method call} or
 * {@link org.spongepowered.asm.mixin.injection.points.BeforeFieldAccess field write}
 * with a conditional check.
 * <p>
 * Your handler method receives the targeted instruction's arguments (optionally followed by the enclosing method's
 * parameters), and should return a boolean indicating whether the operation should go ahead:
 * <table width="100%">
 *   <tr>
 *     <th width="25%">Targeted operation</th>
 *     <th>Handler signature</th>
 *   </tr>
 *   <tr>
 *     <td>Non-static method call</td>
 *     <td><code>private (static) boolean handler(<b>ReceiverType</b> instance, <b>&lt;params of the original
 *     call&gt;</b>)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Static method call</td>
 *     <td><code>private (static) boolean handler(<b>&lt;params of the original call&gt;</b>)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Non-static field write</td>
 *     <td><code>private (static) boolean handler(<b>ReceiverType</b> instance, <b>FieldType</b> newValue)
 *   </tr>
 *   <tr>
 *     <td>Static field write</td>
 *     <td><code>private (static) boolean handler(<b>FieldType</b> newValue)</code></td>
 *   </tr>
 * </table>
 * This chains when used by multiple people, unlike
 * {@link org.spongepowered.asm.mixin.injection.Redirect @Redirect}.
 * <p>
 * <b>If you always return false then you risk other people's code being silently ignored.</b>
 * <p>
 * See <a href="https://github.com/LlamaLad7/MixinExtras/wiki/WrapWithCondition">the wiki article</a> for more info.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WrapWithCondition {
    String[] method();

    At[] at();

    Slice[] slice() default {};

    boolean remap() default true;

    int require() default -1;

    int expect() default 1;

    int allow() default -1;
}
