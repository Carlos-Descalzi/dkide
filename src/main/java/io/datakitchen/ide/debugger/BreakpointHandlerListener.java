package io.datakitchen.ide.debugger;

import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

@SuppressWarnings("rawtypes")
public interface BreakpointHandlerListener {
    void breakpointAdded(XLineBreakpoint breakpoint);
    void breakpointRemoved(XLineBreakpoint breakpoint);
}
