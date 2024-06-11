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
 * Allows you to capture local variables wherever you need them. The annotated parameter's type must either match the
 * target variable's type, or be the corresponding
 * {@link com.llamalad7.mixinextras.sugar.ref.LocalRef LocalRef}
 * type. In the latter case you can both read from and write to the target variable.
 * <p>
 * Targeting the variables can be done in 2 ways:
 * <ul>
 *     <li><b>Explicit Mode</b>: The variable to target is determined by {@code ordinal}, {@code index} or {@code name}.</li>
 *     <li><b>Implicit Mode</b>: You don't specify any of the above. If there is exactly one variable of the targeted type
 *     available, that will be targeted. If not, an error is thrown.</li>
 * </ul>
 * See <a href="https://github.com/LlamaLad7/MixinExtras/wiki/Local">the wiki article</a> for more info.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface Local {
    /**
     * Whether to print a table of the available variables so you can determine the right discriminators to use.
     * <b>This will abort the injection.</b>
     */
    boolean print() default false;

    /**
     * The index of the local variable by type. E.g. if there are 3 {@code String} variables, an ordinal of 2 would
     * target the 3rd one.
     */
    int ordinal() default -1;

    /**
     * The LVT index of the local variable. Get this from the bytecode or using {@link Local#print}. This is generally
     * more brittle than {@code ordinal}.
     */
    int index() default -1;

    /**
     * Names of the local variable. <b>This will not work on obfuscated code like Minecraft</b>. For targeting
     * unobfuscated code this is normally the least brittle option.
     */
    String[] name() default {};

    /**
     * Whether only the method's parameters should be included. This makes the capture more efficient.
     */
    boolean argsOnly() default false;
}
