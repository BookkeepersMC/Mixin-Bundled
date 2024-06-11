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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.util.Bytecode;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class InjectorUtils {
    private static final MixinExtrasLogger LOGGER = MixinExtrasLogger.get("InjectorUtils");

    public static boolean isVirtualRedirect(InjectionNode node) {
        return node.isReplaced() && node.hasDecoration("redirector") && node.getCurrentTarget().getOpcode() != Opcodes.INVOKESTATIC;
    }

    public static boolean isDynamicInstanceofRedirect(InjectionNode node) {
        AbstractInsnNode originalTarget = node.getOriginalTarget();
        AbstractInsnNode currentTarget = node.getCurrentTarget();

        return originalTarget.getOpcode() == Opcodes.INSTANCEOF
                && currentTarget instanceof MethodInsnNode
                && Type.getReturnType(((MethodInsnNode) currentTarget).desc).equals(Type.getType(Class.class));
    }

    public static void checkForDupedNews(Map<Target, List<InjectionNode>> targets) {
        for (Map.Entry<Target, List<InjectionNode>> entry : targets.entrySet()) {
            for (InjectionNode node : entry.getValue()) {
                AbstractInsnNode currentTarget = node.getCurrentTarget();
                if (currentTarget.getOpcode() == Opcodes.NEW) {
                    if (currentTarget.getNext().getOpcode() == Opcodes.DUP) {
                        node.decorate(Decorations.NEW_IS_DUPED, true);
                    }
                }
            }
        }
    }

    public static boolean isDupedNew(InjectionNode node) {
        AbstractInsnNode currentTarget = node.getCurrentTarget();
        return currentTarget != null && currentTarget.getOpcode() == Opcodes.NEW && node.hasDecoration(Decorations.NEW_IS_DUPED);
    }

    public static boolean isDupedFactoryRedirect(InjectionNode node) {
        AbstractInsnNode originalTarget = node.getOriginalTarget();
        return node.isReplaced() && originalTarget.getOpcode() == Opcodes.NEW && !node.hasDecoration(Decorations.WRAPPED) && node.hasDecoration(Decorations.NEW_IS_DUPED);
    }

    public static AbstractInsnNode findFactoryRedirectThrowString(Target target, AbstractInsnNode start) {
        for (ListIterator<AbstractInsnNode> it = target.insns.iterator(target.indexOf(start)); it.hasNext(); ) {
            AbstractInsnNode insn = it.next();

            if (insn instanceof LdcInsnNode) {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst instanceof String && ((String) ldc.cst).startsWith("@Redirect constructor handler ")) {
                    return ldc;
                }
            }
        }
        LOGGER.warn(
                "Please inform LlamaLad7! Failed to find factory redirect throw string for {}",
                Bytecode.describeNode(start)
        );
        return null;
    }

    public static void checkForImmediatePops(Map<Target, List<InjectionNode>> targets) {
        for (List<InjectionNodes.InjectionNode> nodeList : targets.values()) {
            for (InjectionNodes.InjectionNode node : nodeList) {
                AbstractInsnNode currentTarget = node.getCurrentTarget();
                if (currentTarget instanceof MethodInsnNode) {
                    Type returnType = Type.getReturnType(((MethodInsnNode) currentTarget).desc);
                    if (isTypePoppedByInstruction(returnType, currentTarget.getNext())) {
                        node.decorate(Decorations.POPPED_OPERATION, true);
                    }
                }
            }
        }
    }

    private static boolean isTypePoppedByInstruction(Type type, AbstractInsnNode insn) {
        switch (type.getSize()) {
            case 2:
                return insn.getOpcode() == Opcodes.POP2;
            case 1:
                return insn.getOpcode() == Opcodes.POP;
            default:
                return false;
        }
    }
}
