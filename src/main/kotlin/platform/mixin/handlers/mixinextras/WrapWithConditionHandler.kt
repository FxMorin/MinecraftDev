/*
 * Minecraft Development for IntelliJ
 *
 * https://mcdev.io/
 *
 * Copyright (C) 2024 minecraft-dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, version 3.0 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.demonwav.mcdev.platform.mixin.handlers.mixinextras

import com.demonwav.mcdev.platform.mixin.inspection.injector.ParameterGroup
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.llamalad7.mixinextras.expression.impl.point.ExpressionContext
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class WrapWithConditionHandler : MixinExtrasInjectorAnnotationHandler() {
    override val oldSuperBehavior = true

    override val supportedInstructionTypes = listOf(
        InstructionType.METHOD_CALL, InstructionType.FIELD_SET
    )

    override fun extraTargetRestrictions(insn: AbstractInsnNode) = getInsnReturnType(insn) == Type.VOID_TYPE

    override fun expectedMethodSignature(
        annotation: PsiAnnotation,
        targetClass: ClassNode,
        targetMethod: MethodNode,
        target: TargetInsn
    ): Pair<ParameterGroup, PsiType>? {
        val params = getPsiParameters(target.insn, targetClass, annotation) ?: return null
        return ParameterGroup(params) to PsiTypes.booleanType()
    }

    override val mixinExtrasExpressionContextType = ExpressionContext.Type.WRAP_WITH_CONDITION
}
