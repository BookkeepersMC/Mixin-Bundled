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

import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefClassGenerator;
import com.llamalad7.mixinextras.sugar.impl.ref.LocalRefUtils;
import com.llamalad7.mixinextras.utils.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.struct.Target;

import java.util.List;

public class ShareType {
    private final Type innerType;

    public ShareType(Type innerType) {
        this.innerType = innerType;
    }

    public Type getInnerType() {
        return innerType;
    }

    public Type getImplType() {
        return Type.getObjectType(LocalRefClassGenerator.getForType(innerType));
    }

    public InsnList initialize(int lvtIndex) {
        InsnList init = new InsnList();
        LocalRefUtils.generateNew(init, innerType);
        init.add(new VarInsnNode(Opcodes.ASTORE, lvtIndex));
        init.add(new VarInsnNode(Opcodes.ALOAD, lvtIndex));
        init.add(new InsnNode(ASMUtils.getDummyOpcodeForType(innerType)));
        LocalRefUtils.generateInitialization(init, innerType);
        return init;
    }

    public void addToLvt(Target target, int lvtIndex) {
        LabelNode start = new LabelNode();
        target.insns.insert(start);
        LabelNode end = new LabelNode();
        target.insns.add(end);
        Type implType = Type.getObjectType(LocalRefClassGenerator.getForType(innerType));

        target.addLocalVariable(lvtIndex, "sharedRef" + lvtIndex, implType.getDescriptor());
        List<LocalVariableNode> lvt = target.method.localVariables;
        LocalVariableNode newVar = lvt.get(lvt.size() - 1);
        newVar.start = start;
        newVar.end = end;
    }
}
