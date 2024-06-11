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
package com.llamalad7.mixinextras.injector.wrapoperation;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Slice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows you to wrap a
 * {@link org.spongepowered.asm.mixin.injection.points.BeforeInvoke method call},
 * {@link org.spongepowered.asm.mixin.injection.points.BeforeFieldAccess field get/set},
 * {@link org.spongepowered.asm.mixin.injection.Constant <code>instanceof</code> check}, or
 * {@link org.spongepowered.asm.mixin.injection.points.BeforeNew object instantiation}.
 * <p>
 * Your handler method receives the targeted instruction's arguments and an {@link Operation} representing the operation
 * being wrapped (optionally followed by the enclosing method's parameters).
 * You should return the same type as the wrapped operation does:
 * <table width="100%">
 *   <tr>
 *     <th width="25%">Targeted operation</th>
 *     <th>Handler signature</th>
 *   </tr>
 *   <tr>
 *     <td>Non-static method call</td>
 *     <td><code>private (static) <b>ReturnType</b> handler(<b>ReceiverType</b> instance, <b>&lt;params of the original
 *     call&gt;</b>, Operation&lt;<b>ReturnType</b>&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>super.</code> method call</td>
 *     <td><code>private (static) <b>ReturnType</b> handler(<b>ThisClass</b> instance, <b>&lt;params of the original
 *     call&gt;</b>, Operation&lt;<b>ReturnType</b>&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Static method call</td>
 *     <td><code>private (static) <b>ReturnType</b> handler(<b>&lt;params of the original call&gt;</b>,
 *     Operation&lt;<b>ReturnType</b>&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Non-static field get</td>
 *     <td><code>private (static) <b>FieldType</b> handler(<b>ReceiverType</b> instance,
 *     Operation&lt;<b>FieldType</b>&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Static field get</td>
 *     <td><code>private (static) <b>FieldType</b> handler(Operation&lt;<b>FieldType</b>&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Non-static field write</td>
 *     <td><code>private (static) void handler(<b>ReceiverType</b> instance, <b>FieldType</b> newValue,
 *     Operation&lt;Void&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Static field write</td>
 *     <td><code>private (static) void handler(<b>FieldType</b> newValue, Operation&lt;Void&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>instanceof</code> check</td>
 *     <td><code>private (static) boolean handler(Object obj, Operation&lt;Boolean&gt; original)</code></td>
 *   </tr>
 *   <tr>
 *     <td>Object instantiation</td>
 *     <td><code>private (static) <b>ObjectType</b> handler(<b>&lt;params of the original ctor&gt;</b>,
 *     Operation&lt;<b>ObjectType</b>&gt; original)</code></td>
 *   </tr>
 * </table>
 * When {@code call}ing the {@code original}, you must pass everything before the {@code original} in your handler's
 * parameters. You can optionally pass different values to change what the {@code original} uses.
 * <p>
 * This chains when used by multiple people, unlike
 * {@link org.spongepowered.asm.mixin.injection.Redirect @Redirect} and
 * {@link org.spongepowered.asm.mixin.injection.ModifyConstant @ModifyConstant}.
 * <p>
 * <b>If you never call the {@code original} then you risk other people's code being silently ignored.</b>
 * <p>
 * See <a href="https://github.com/LlamaLad7/MixinExtras/wiki/WrapOperation">the wiki article</a> for more info.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WrapOperation {
    String[] method();

    /**
     * Selector for targeting method calls, field gets/sets and object instantiations.
     */
    At[] at() default {};

    /**
     * Selector for targeting `instanceof`s.
     */
    Constant[] constant() default {};

    Slice[] slice() default {};

    boolean remap() default true;

    int require() default -1;

    int expect() default 1;

    int allow() default -1;
}
