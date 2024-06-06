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
package org.spongepowered.tools.obfuscation.mapping;

import java.util.LinkedHashSet;

import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.ObfuscationType;

import com.google.common.base.Objects;

/**
 * Mapping consumers collect the mappings generated by an AP pass ready for them
 * to be written to the output files
 */
public interface IMappingConsumer {
    
    /**
     * A set of mapping pairs
     * 
     * @param <TMapping> type of mappings in the set
     */
    public class MappingSet<TMapping extends IMapping<TMapping>> extends LinkedHashSet<MappingSet.Pair<TMapping>> {
        
        /**
         * A pair of mappings
         * 
         * @param <TMapping> The type of mappings being mapped
         */
        public static class Pair<TMapping extends IMapping<TMapping>> {

            /**
             * Source mapping
             */
            public final TMapping from;
            
            /**
             * Destination mapping
             */
            public final TMapping to;

            public Pair(TMapping from, TMapping to) {
                this.from = from;
                this.to = to;
            }
            
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof Pair)) {
                    return false;
                }
                
                @SuppressWarnings("unchecked")
                Pair<TMapping> other = (Pair<TMapping>) obj;
                return Objects.equal(this.from, other.from) && Objects.equal(this.to, other.to);
            }
            
            @Override
            public int hashCode() {
                return Objects.hashCode(this.from, this.to);
            }
            
            @Override
            public String toString() {
                return String.format("%s -> %s", this.from, this.to);
            }
            
        }

        private static final long serialVersionUID = 1L;
        
    }

    /**
     * Clear all collections in the consumer
     */
    public abstract void clear();

    /**
     * Add a field mapping to this consumer.
     * 
     * @param type obfuscation type for this entry
     * @param from source mapping
     * @param to destination mapping
     */
    public abstract void addFieldMapping(ObfuscationType type, MappingField from, MappingField to);

    /**
     * Add a method mapping to this consumer.
     * 
     * @param type obfuscation type for this entry
     * @param from source mapping
     * @param to destination mapping
     */
    public abstract void addMethodMapping(ObfuscationType type, MappingMethod from, MappingMethod to);

    /**
     * Get the stored field mappings for the specified obfuscation type
     * 
     * @param type obfuscation type
     * @return stored field mappings
     */
    public abstract MappingSet<MappingField> getFieldMappings(ObfuscationType type);

    /**
     * Get the stored method mappings for the specified obfuscation type
     * 
     * @param type obfuscation type
     * @return stored method mappings
     */
    public abstract MappingSet<MappingMethod> getMethodMappings(ObfuscationType type);

}
