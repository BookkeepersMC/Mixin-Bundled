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

public enum MixinExtrasVersion {
    V0_2_0_BETA_1("0.2.0-beta.1", -9),
    V0_2_0_BETA_2("0.2.0-beta.2", -8),
    V0_2_0_BETA_3("0.2.0-beta.3", -7),
    V0_2_0_BETA_4("0.2.0-beta.4", -6),
    V0_2_0_BETA_5("0.2.0-beta.5", -5),
    V0_2_0_BETA_6("0.2.0-beta.6", -4),
    V0_2_0_BETA_7("0.2.0-beta.7", -3),
    V0_2_0_BETA_8("0.2.0-beta.8", -2),
    V0_2_0_BETA_9("0.2.0-beta.9", -1),
    V0_3_4("0.3.4", 213),
    V0_4_0_BETA_1("0.4.0-beta.1", 315),
    V0_4_0_BETA_2("0.4.0-beta.2", 316),
    ;

    public static final MixinExtrasVersion LATEST = values()[values().length - 1];

    private final String prettyName;
    private final int versionNumber;

    MixinExtrasVersion(String prettyName, int versionNumber) {
        this.prettyName = prettyName;
        this.versionNumber = versionNumber;
    }

    @Override
    public String toString() {
        return prettyName;
    }

    public int getNumber() {
        return versionNumber;
    }
}
