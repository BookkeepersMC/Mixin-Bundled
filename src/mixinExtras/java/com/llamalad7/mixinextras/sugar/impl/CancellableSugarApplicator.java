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

import com.llamalad7.mixinextras.injector.StackExtension;
import com.llamalad7.mixinextras.utils.ASMUtils;
import com.llamalad7.mixinextras.utils.Decorations;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

class CancellableSugarApplicator extends SugarApplicator {
    CancellableSugarApplicator(InjectionInfo info, SugarParameter parameter) {
        super(info, parameter);
    }

    @Override
    void validate(Target target, InjectionNode node) {
    }

    @Override
    void prepare(Target target, InjectionNode node) {
    }

    @Override
    void inject(Target target, InjectionNode node, StackExtension stack) {
        Type ciType = Type.getObjectType(target.getCallbackInfoClass());
        if (!ciType.equals(paramType)) {
            throw new IllegalStateException(
                    String.format(
                            "@Cancellable sugar has wrong type! Expected %s but got %s!",
                            ciType.getClassName(),
                            paramType.getClassName()
                    )
            );
        }
        int ciIndex = getOrCreateCi(target, node, stack, ciType);
        stack.extra(1);
        target.insns.insertBefore(node.getCurrentTarget(), new VarInsnNode(Opcodes.ALOAD, ciIndex));
    }

    @Override
    int postProcessingPriority() {
        // Early, we don't care about being particularly tight compared to e.g. `@Local`s.
        return -1000;
    }

    private int getOrCreateCi(Target target, InjectionNode node, StackExtension stack, Type ciType) {
        if (node.hasDecoration(Decorations.CANCELLABLE_CI_INDEX)) {
            return node.getDecoration(Decorations.CANCELLABLE_CI_INDEX);
        }
        int ciIndex = target.allocateLocal();
        target.addLocalVariable(ciIndex, "callbackInfo" + ciIndex, ciType.getDescriptor());
        node.decorate(Decorations.CANCELLABLE_CI_INDEX, ciIndex);

        InsnList init = new InsnList();
        init.add(new TypeInsnNode(Opcodes.NEW, ciType.getInternalName()));
        init.add(new InsnNode(Opcodes.DUP));
        init.add(new LdcInsnNode(target.method.name));
        init.add(new InsnNode(Opcodes.ICONST_1));
        init.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                ciType.getInternalName(),
                "<init>",
                "(Ljava/lang/String;Z)V",
                false
        ));
        init.add(new VarInsnNode(Opcodes.ASTORE, ciIndex));
        target.insertBefore(node, init);
        stack.extra(4);

        SugarPostProcessingExtension.enqueuePostProcessing(this, () -> {
            InsnList cancellation = new InsnList();
            LabelNode notCancelled = new LabelNode();
            cancellation.add(new VarInsnNode(Opcodes.ALOAD, ciIndex));
            cancellation.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    ciType.getInternalName(),
                    "isCancelled",
                    "()Z",
                    false
            ));
            cancellation.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));
            cancellation.add(new VarInsnNode(Opcodes.ALOAD, ciIndex));
            if (target.returnType.equals(Type.VOID_TYPE)) {
                cancellation.add(new InsnNode(Opcodes.RETURN));
            } else if (ASMUtils.isPrimitive(target.returnType)) {
                cancellation.add(new MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        ciType.getInternalName(),
                        "getReturnValue" + target.returnType.getDescriptor(),
                        "()" + target.returnType.getDescriptor(),
                        false
                ));
                cancellation.add(new InsnNode(target.returnType.getOpcode(Opcodes.IRETURN)));
            } else {
                cancellation.add(new MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        ciType.getInternalName(),
                        "getReturnValue",
                        "()Ljava/lang/Object;",
                        false
                ));
                cancellation.add(new TypeInsnNode(Opcodes.CHECKCAST, target.returnType.getInternalName()));
                cancellation.add(new InsnNode(Opcodes.ARETURN));
            }
            cancellation.add(notCancelled);
            target.insns.insert(node.getCurrentTarget(), cancellation);
            // No need to adjust the stack because we only increase the height by at most 2, which is covered by
            // our bump of 4 earlier.
        });
        return ciIndex;
    }
}
