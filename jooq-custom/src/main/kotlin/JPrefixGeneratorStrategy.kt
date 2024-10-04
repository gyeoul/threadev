package jooq.custom.generator

import org.jooq.codegen.GeneratorStrategy.*
import org.jooq.meta.Definition

open class JPrefixGeneratorStrategy : org.jooq.codegen.DefaultGeneratorStrategy() {
    override fun getJavaClassName(definition: Definition, mode: Mode): String = when (mode) {
        Mode.DEFAULT -> "J" + super.getJavaClassName(definition, mode)
        else -> super.getJavaClassName(definition, mode)
    }
}
