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
package com.llamalad7.mixinextras.sugar.ref;

/**
 * Represents a reference to a local variable.
 * Specialised variants are provided in the same package for all primitive types.
 * Do not store {@link LocalRef} instances - their lifecycle is an implementation detail.
 * They will be valid throughout the handler method they are passed to, but no other guarantees are made.
 * Do not create custom {@link LocalRef} implementations - methods may be added in the future.
 * @param <T> the type of the local variable - MUST be concrete
 */
public interface LocalRef<T> {
    /**
     * Gets the current value of the variable. This may change between calls, even in the same handler method.
     * @return the variable's current value
     */
    T get();

    /**
     * Sets the value of the variable. This value will be written back to the target method.
     * @param value a new value for the variable
     */
    void set(T value);
}
