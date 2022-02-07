package io.datakitchen.ide.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes","unchecked"})
public class BreakpointHandler extends XBreakpointHandler {

    private final Map<String, XLineBreakpoint> breakpoints = new LinkedHashMap<>();
    private final BreakpointHandlerListener listener;
    public BreakpointHandler(BreakpointHandlerListener listener, Class<? extends XBreakpointType> breakpointType) {
        super(breakpointType);
        this.listener = listener;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void registerBreakpoint(@NotNull XBreakpoint breakpoint) {
        XLineBreakpoint lineBreakpoint = (XLineBreakpoint)breakpoint;

        breakpoints.put(makeKey(lineBreakpoint),lineBreakpoint);
        this.listener.breakpointAdded(lineBreakpoint);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void unregisterBreakpoint(@NotNull XBreakpoint breakpoint, boolean temporary) {
        XLineBreakpoint lineBreakpoint = (XLineBreakpoint)breakpoint;

        breakpoints.put(makeKey(lineBreakpoint),lineBreakpoint);
        this.listener.breakpointRemoved(lineBreakpoint);
    }

    @SuppressWarnings("rawtypes")
    private String makeKey(XLineBreakpoint lineBreakpoint) {
        return lineBreakpoint.getFileUrl()+":"+lineBreakpoint.getLine();
    }

    @SuppressWarnings("rawtypes")
    public XLineBreakpoint getBreakpoint(String fileName, int line) {
        return breakpoints.get(fileName+"."+line);
    }
}
