package io.datakitchen.ide.debugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

public class SuspendContext extends XSuspendContext {

    private final ExecutionStack activeExecutionStack;
    public SuspendContext(ExecutionStack stack){
        this.activeExecutionStack = stack;
    }
    public XExecutionStack getActiveExecutionStack() {
        return activeExecutionStack;
    }
}
