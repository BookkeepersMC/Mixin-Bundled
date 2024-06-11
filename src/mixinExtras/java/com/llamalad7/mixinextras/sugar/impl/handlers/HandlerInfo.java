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
package com.llamalad7.mixinextras.sugar.impl.handlers;

import com.llamalad7.mixinextras.sugar.impl.SugarParameter;
import com.llamalad7.mixinextras.utils.ASMUtils;
import com.llamalad7.mixinextras.utils.UniquenessHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.util.Bytecode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Information about a sugared handler method. Can be transformed by {@link HandlerTransformer}s.
 * If transformations are required, a new bridge handler will be created with the required changes, and the original
 * will become an ordinary method which the bridge delegates to.
 */
public class HandlerInfo {
    private final Map<Integer, ParameterWrapper> wrappers = new LinkedHashMap<>();

    private static class ParameterWrapper {
        private final Type type;
        private final Type generic;
        private final BiConsumer<InsnList, Runnable> unwrap;

        private ParameterWrapper(Type type, Type generic, BiConsumer<InsnList, Runnable> unwrap) {
            this.type = type;
            this.generic = generic;
            this.unwrap = unwrap;
        }
    }

    public void wrapParameter(SugarParameter param, Type type, Type generic, BiConsumer<InsnList, Runnable> unwrap) {
        wrappers.put(param.paramIndex, new ParameterWrapper(type, generic, unwrap));
    }

    public void transformHandler(ClassNode targetClass, MethodNode handler) {
        Type[] paramTypes = Type.getArgumentTypes(handler.desc);
        InsnList insns = new InsnList();

        if (!Bytecode.isStatic(handler)) {
            insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        int index = Bytecode.isStatic(handler) ? 0 : 1;
        for (int i = 0; i < paramTypes.length; i++) {
            VarInsnNode loadInsn = new VarInsnNode(paramTypes[i].getOpcode(Opcodes.ILOAD), index);

            ParameterWrapper wrapper = wrappers.get(i);
            if (wrapper != null) {
                paramTypes[i] = wrapper.type;
                loadInsn.setOpcode(wrapper.type.getOpcode(Opcodes.ILOAD));
                wrapper.unwrap.accept(insns, () -> insns.add(loadInsn));
            } else {
                insns.add(loadInsn);
            }

            index += paramTypes[i].getSize();
        }
        insns.add(ASMUtils.getInvokeInstruction(targetClass, handler));
        insns.add(new InsnNode(Type.getReturnType(handler.desc).getOpcode(Opcodes.IRETURN)));

        handler.instructions = insns;
        handler.localVariables = null;
        handler.name = UniquenessHelper.getUniqueMethodName(targetClass, handler.name + "$mixinextras$bridge");
        handler.desc = Type.getMethodDescriptor(Type.getReturnType(handler.desc), paramTypes);
    }

    public void transformGenerics(ArrayList<Type> generics) {
        for (Map.Entry<Integer, ParameterWrapper> entry : wrappers.entrySet()) {
            Type type = entry.getValue().generic;
            generics.set(entry.getKey(), type);
        }
    }
}
