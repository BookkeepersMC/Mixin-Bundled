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
package com.llamalad7.mixinextras.sugar.impl.ref;

import com.llamalad7.mixinextras.service.MixinExtrasService;
import com.llamalad7.mixinextras.service.MixinExtrasVersion;
import com.llamalad7.mixinextras.sugar.impl.ref.generated.GeneratedImplDummy;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.llamalad7.mixinextras.utils.ClassGenUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * We must generate implementations of {@link LocalRef} and co. at runtime so they implement all the required interfaces.
 * These objects will be shared between handlers that possibly use different relocations of MixinExtras.
 */
public class LocalRefClassGenerator {
    private static final String IMPL_PACKAGE = StringUtils.substringBeforeLast(LocalRefClassGenerator.class.getName(), ".").replace('.', '/') + "/generated";
    private static final Map<Class<?>, String> interfaceToImpl = new HashMap<>();

    public static String getForType(Type type) {
        Class<?> refInterface = LocalRefUtils.getInterfaceFor(type);
        String owner = interfaceToImpl.get(refInterface);
        if (owner != null) {
            return owner;
        }
        owner = IMPL_PACKAGE + '/' + StringUtils.substringAfterLast(refInterface.getName(), ".") + "Impl";
        String desc = type.getDescriptor();
        String innerDesc = desc.length() == 1 ? desc : Type.getDescriptor(Object.class);
        interfaceToImpl.put(refInterface, owner);
        ClassNode node = new ClassNode();
        node.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_FINAL,
                owner,
                null,
                Type.getInternalName(Object.class),
                null
        );
        generateClass(node, owner, innerDesc, refInterface.getName());
        ClassGenUtils.defineClass(node, GeneratedImplDummy.getLookup());
        return owner;
    }

    private static void generateClass(ClassNode node, String owner, String innerDesc, String interfaceName) {
        Type objectType = Type.getType(Object.class);
        Type innerType = Type.getType(innerDesc);

        for (String name : MixinExtrasService.getInstance().getAllClassNamesAtLeast(interfaceName, MixinExtrasVersion.V0_2_0_BETA_5)) {
            node.interfaces.add(name.replace('.', '/'));
        }
        node.visitField(Opcodes.ACC_PRIVATE, "value", innerDesc, null, null);
        node.visitField(Opcodes.ACC_PRIVATE, "state", "B", null, null);

        Consumer<InstructionAdapter> checkState = code -> {
            String runtime = Type.getInternalName(LocalRefRuntime.class);
            code.load(0, objectType);
            code.getfield(owner, "state", "B");
            Label passed = new Label();
            code.ifeq(passed);
            code.load(0, objectType);
            code.getfield(owner, "state", "B");
            code.invokestatic(runtime, "checkState", "(B)V", false);
            code.mark(passed);
        };

        genMethod(node, "<init>", "()V", code -> {
            code.load(0, objectType);
            code.invokespecial(objectType.getInternalName(), "<init>", "()V", false);
            code.load(0, objectType);
            code.iconst(LocalRefRuntime.UNINITIALIZED);
            code.putfield(owner, "state", "B");
            code.areturn(Type.VOID_TYPE);
        });

        genMethod(node, "get", "()" + innerDesc, code -> {
            checkState.accept(code);
            code.load(0, objectType);
            code.getfield(owner, "value", innerDesc);
            code.areturn(innerType);
        });

        genMethod(node, "set", "(" + innerDesc + ")V", code -> {
            checkState.accept(code);
            code.load(0, objectType);
            code.load(1, innerType);
            code.putfield(owner, "value", innerDesc);
            code.areturn(Type.VOID_TYPE);
        });

        genMethod(node, "init", "(" + innerDesc + ")V", code -> {
            code.load(0, objectType);
            code.load(1, innerType);
            code.putfield(owner, "value", innerDesc);
            code.load(0, objectType);
            code.iconst(0);
            code.putfield(owner, "state", "B");
            code.areturn(Type.VOID_TYPE);
        });

        genMethod(node, "dispose", "()" + innerDesc, code -> {
            checkState.accept(code);
            code.load(0, objectType);
            code.iconst(LocalRefRuntime.DISPOSED);
            code.putfield(owner, "state", "B");
            code.load(0, objectType);
            code.getfield(owner, "value", innerDesc);
            code.areturn(innerType);
        });
    }

    private static void genMethod(ClassVisitor cv, String name, String desc, Consumer<InstructionAdapter> code) {
        code.accept(new InstructionAdapter(cv.visitMethod(Opcodes.ACC_PUBLIC, name, desc, null, null)));
    }
}
