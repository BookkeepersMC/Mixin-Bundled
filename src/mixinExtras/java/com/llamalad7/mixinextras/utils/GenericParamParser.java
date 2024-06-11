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

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.spongepowered.asm.util.asm.ASM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericParamParser extends SignatureVisitor {
    private final List<Type> results = new ArrayList<>();

    private GenericParamParser() {
        super(ASM.API_VERSION);
    }

    public static List<Type> getParameterGenerics(String desc, String signature) {
        if (signature == null || signature.isEmpty()) {
            return Collections.nCopies(Type.getArgumentTypes(desc).length, null);
        }
        GenericParamParser parser = new GenericParamParser();
        new SignatureReader(signature).accept(parser);
        return parser.results;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        int index = results.size();
        results.add(null);
        return new SignatureVisitor(api) {
            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                if (wildcard != SignatureVisitor.INSTANCEOF) {
                    return this;
                }
                return new SignatureVisitor(api) {
                    private int depth;
                    private int arrayDimensions;
                    private String internalName;

                    @Override
                    public SignatureVisitor visitArrayType() {
                        if (depth == 0) {
                            arrayDimensions++;
                        }
                        return this;
                    }

                    @Override
                    public void visitBaseType(char descriptor) {
                        if (depth == 0) {
                            results.set(index, Type.getType(StringUtils.repeat('[', arrayDimensions) + descriptor));
                        }
                    }

                    @Override
                    public void visitClassType(String name) {
                        if (++depth == 1) {
                            internalName = name;
                        }
                    }

                    @Override
                    public void visitInnerClassType(String name) {
                        if (depth == 1) {
                            internalName += '$' + name;
                        }
                    }

                    @Override
                    public void visitEnd() {
                        depth--;
                        String prefix = StringUtils.repeat('[', arrayDimensions);
                        results.set(index, Type.getType(prefix + (Type.getObjectType(internalName).getDescriptor())));
                    }
                };
            }
        };
    }
}
