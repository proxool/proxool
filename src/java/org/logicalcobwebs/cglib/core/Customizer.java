package org.logicalcobwebs.cglib.core;

import org.logicalcobwebs.asm.Type;

public interface Customizer {
    void customize(CodeEmitter e, Type type);
}
