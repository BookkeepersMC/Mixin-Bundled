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

import com.llamalad7.mixinextras.injector.MixinExtrasLateInjectionInfo;
import com.llamalad7.mixinextras.utils.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.points.BeforeConstant;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.Bytecode;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@InjectionInfo.AnnotationType(WrapOperation.class)
@HandlerPrefix("wrapOperation")
public class WrapOperationInjectionInfo extends MixinExtrasLateInjectionInfo {
    private static final MixinExtrasLogger LOGGER = MixinExtrasLogger.get("WrapOperation");

    public WrapOperationInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
        super(mixin, method, annotation, determineAtKey(mixin, method, annotation));
    }

    @Override
    protected Injector parseInjector(AnnotationNode injectAnnotation) {
        return new WrapOperationInjector(this);
    }

    @Override
    public void prepare() {
        super.prepare();
        InjectorUtils.checkForDupedNews(this.targetNodes);
        for (Map.Entry<Target, List<InjectionNodes.InjectionNode>> entry : this.targetNodes.entrySet()) {
            Target target = entry.getKey();
            for (ListIterator<InjectionNodes.InjectionNode> it = entry.getValue().listIterator(); it.hasNext(); ) {
                InjectionNodes.InjectionNode node = it.next();
                AbstractInsnNode currentTarget = node.getCurrentTarget();
                if (currentTarget.getOpcode() == Opcodes.NEW) {
                    MethodInsnNode initCall = ASMUtils.findInitNodeFor(target, (TypeInsnNode) currentTarget);
                    if (initCall == null) {
                        LOGGER.warn("NEW node {} in {} has no init call?", Bytecode.describeNode(currentTarget), target);
                        it.remove();
                        continue;
                    }
                    node.decorate(Decorations.NEW_ARG_TYPES, Type.getArgumentTypes(initCall.desc));
                }
            }
        }
    }

    @Override
    protected void parseInjectionPoints(List<AnnotationNode> ats) {
        if (this.atKey.equals("at")) {
            super.parseInjectionPoints(ats);
            return;
        }
        // If we're wrapping a `constant`, we need to parse the injection points ourselves.
        Type returnType = Type.getReturnType(this.method.desc);

        for (AnnotationNode at : ats) {
            this.injectionPoints.add(new BeforeConstant(CompatibilityHelper.getMixin(this), at, returnType.getDescriptor()));
        }
    }

    @Override
    public String getLateInjectionType() {
        return "WrapOperation";
    }

    private static String determineAtKey(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
        boolean at = Annotations.getValue(annotation, "at") != null;
        boolean constant = Annotations.getValue(annotation, "constant") != null;
        if (at == constant) {
            throw new IllegalStateException(
                    String.format("@WrapOperation injector %s::%s must specify exactly one of `at` and `constant`, got %s.",
                            mixin.getMixin().getClassName(),
                            method.name,
                            at ? "both" : "neither"
                    )
            );
        } else {
            return at ? "at" : "constant";
        }
    }
}
