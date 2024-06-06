/*
 * This file is part of Mixin, licensed under the MIT License (MIT).
 *
 * Copyright (c) BookkeepersMC <https://www.spongepowered.org>
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
package org.spongepowered.asm.mixin.transformer;

import java.lang.reflect.Modifier;
import java.util.Map.Entry;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Feature;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.transformer.ClassInfo.Field;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidInterfaceMixinException;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.Constants;
import org.spongepowered.asm.util.LanguageFeatures;

/**
 * Applicator for interface mixins, mainly just disables things which aren't
 * supported for interface mixins
 */
class MixinApplicatorInterface extends MixinApplicatorStandard {

    /**
     * ctor
     * 
     * @param context applier context
     */
    MixinApplicatorInterface(TargetClassContext context) {
        super(context);
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.asm.mixin.transformer.MixinApplicator
     *      #applyInterfaces(
     *      org.spongepowered.asm.mixin.transformer.MixinTargetContext)
     */
    @Override
    protected void applyInterfaces(MixinTargetContext mixin) {
        for (String interfaceName : mixin.getInterfaces()) {
            if (!this.targetClass.name.equals(interfaceName) && !this.targetClass.interfaces.contains(interfaceName)) {
                this.targetClass.interfaces.add(interfaceName);
                mixin.getTargetClassInfo().addInterface(interfaceName);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.mixin.transformer.MixinApplicator
     *      #applyFields(
     *      org.spongepowered.asm.mixin.transformer.MixinTargetContext)
     */
    @Override
    protected void mergeShadowFields(MixinTargetContext mixin) {
        // shadow fields have no meaning for interfaces, just spam the dev with messages
        for (Entry<FieldNode, Field> entry : mixin.getShadowFields()) {
            FieldNode shadow = entry.getKey();
            FieldNode target = this.findTargetField(shadow);

            if (target != null) {
                Annotations.merge(shadow, target);

                if (entry.getValue().isDecoratedMutable()) {
                    this.logger.error("Ignoring illegal @Mutable on {}:{} in {}", shadow.name, shadow.desc, mixin);
                }

                if (shadow.value != null) {
                    this.logger.warn("@Shadow field {}:{} in {} has an inlinable valie set, is this intended?", shadow.name, shadow.desc, mixin);
                }

            } else {
                this.logger.warn("Unable to find target for @Shadow {}:{} in {}", shadow.name, shadow.desc, mixin);
            }
        }
    }

    @Override
    protected void mergeNewFields(MixinTargetContext mixin) {
        //Disabled for interface mixins
    }

    @Override
    protected void applyNormalMethod(MixinTargetContext mixin, MethodNode mixinMethod) {
        if (!Constants.CLINIT.equals(mixinMethod.name)) {
            super.applyNormalMethod(mixin, mixinMethod);
        }
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.mixin.transformer.MixinApplicator
     *      #applyInitialisers(
     *      org.spongepowered.asm.mixin.transformer.MixinTargetContext)
     */
    @Override
    protected void applyInitialisers(MixinTargetContext mixin) {
        // disabled for interface mixins
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.asm.mixin.transformer.MixinApplicator
     *      #prepareInjections(
     *      org.spongepowered.asm.mixin.transformer.MixinTargetContext)
     */
    @Override
    protected void prepareInjections(MixinTargetContext mixin) {
        if (Feature.INJECTORS_IN_INTERFACE_MIXINS.isEnabled()) {
            try {
                super.prepareInjections(mixin);
            } catch (InvalidInjectionException ex) {
                String description = ex.getContext() != null ? ex.getContext().toString() : "Injection";
                throw new InvalidInterfaceMixinException(mixin, description + " is not supported in interface mixin", ex);
            }
            return;
        }

        InjectionInfo injectInfo = mixin.getFirstInjectionInfo();

        if (injectInfo != null) {
            if (!MixinEnvironment.getCompatibilityLevel().supports(LanguageFeatures.METHODS_IN_INTERFACES)) {
                throw new InvalidInterfaceMixinException(mixin, injectInfo + " is not supported on interface mixin method " + injectInfo.getMethodName());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.asm.mixin.transformer.MixinApplicator
     *      #applyPreInjections(
     *      org.spongepowered.asm.mixin.transformer.MixinTargetContext)
     */
    @Override
    protected void applyPreInjections(MixinTargetContext mixin) {
        if (Feature.INJECTORS_IN_INTERFACE_MIXINS.isEnabled()) {
            super.applyPreInjections(mixin);
            return;
        }
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.asm.mixin.transformer.MixinApplicator
     *      #applyInjections(
     *      org.spongepowered.asm.mixin.transformer.MixinTargetContext, int)
     */

    @Override
    protected void checkMethodVisibility(MixinTargetContext mixin, MethodNode mixinMethod) {
        if (Modifier.isStatic(mixinMethod.access) && !MixinEnvironment.getCompatibilityLevel().supports(LanguageFeatures.PRIVATE_METHODS_IN_INTERFACES)) {
            InjectionInfo injectInfo = InjectionInfo.parse(mixin, mixinMethod);
            if (injectInfo != null) {
                return;
            }
        }

        super.checkMethodVisibility(mixin, mixinMethod);
    }
}
