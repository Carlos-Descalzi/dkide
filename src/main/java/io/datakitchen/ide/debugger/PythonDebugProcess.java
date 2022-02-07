package io.datakitchen.ide.debugger;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;
import com.intellij.xdebugger.frame.XSuspendContext;
import io.datakitchen.ide.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.function.Consumer;

public class PythonDebugProcess extends XDebugProcess implements DebugServerCallback, BreakpointHandlerListener {

    private static final String PYTHON_CODE_BREAKPOINT_TYPE = "com.jetbrains.python.debugger.PyLineBreakpointType";

    private final DebugServer debugServer;
    private final List<VirtualFile> sourceFolders;
    private final String containerBasePath;
    private final ProcessHandler processHandler;
    private final Project project;
    private BreakpointHandler breakpointHandler;

    public PythonDebugProcess(
            @NotNull XDebugSession session,
            Project project,
            List<VirtualFile> sourceFolders,
            String containerBasePath,
            ProcessHandler processHandler
    ) {
        super(session);
        this.project = project;
        this.sourceFolders = sourceFolders;
        this.containerBasePath = containerBasePath;
        this.debugServer = new DebugServer(this);
        this.processHandler = processHandler;
        try {

            PluginManager mgr = PluginManager.getInstance();
            // no need to check if python plugin is installed since is done on the action that dispatches
            // the debugger
            IdeaPluginDescriptor descriptor = PluginManager
                .getInstance()
                .findEnabledPlugin(PluginId.findId(Constants.PYTHON_PLUGIN_ID));

            Class<? extends XBreakpointType> breakpointType = getBreakpointType(descriptor);

            this.breakpointHandler = new BreakpointHandler(this, breakpointType);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private Class<? extends XBreakpointType> getBreakpointType(IdeaPluginDescriptor descriptor) throws ClassNotFoundException {
        /*
         I can't just reference python plugin classes even if I place them in this plugin, because
         these are actually in a different class loader.
        */
        return (Class<? extends XBreakpointType>) descriptor
            .getPluginClassLoader()
            .loadClass(PYTHON_CODE_BREAKPOINT_TYPE);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void breakpointAdded(XLineBreakpoint breakpoint) {
        debugServer.sendMessage(Message.setBreak(adjustFileName(breakpoint.getFileUrl()),breakpoint.getLine()+1));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void breakpointRemoved(XLineBreakpoint breakpoint) {
        debugServer.sendMessage(Message.removeBreak(adjustFileName(breakpoint.getFileUrl()),breakpoint.getLine()+1));
    }

    private String adjustFileName(String fileUrl) {
        for (VirtualFile sourceFolder: sourceFolders){
            if (fileUrl.contains(sourceFolder.getUrl())){
                return Constants.DOCKER_SHARE_FOLDER+"/"+fileUrl.substring(sourceFolder.getUrl().length()+1);
            }
        }
        return fileUrl;
    }

    @Override
    public void messageReceived(Message message) {

        if (message.getNumber() == Message.MSG_THREAD_SUSPEND) {
            try {
                Document doc = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(new InputSource(new StringReader(message.getPayload())));
                Node rootNode = doc.getDocumentElement().getFirstChild();
                ExecutionStack stack = ExecutionStack.fromXml(this, sourceFolders, containerBasePath, rootNode);
                setExecutionStack(stack);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (message.getNumber() == Message.MSG_THREAD_RUN){
            System.out.println("Thread "+message.getPayload()+" resumed");
        } else {
            System.out.println("Received message "+message);
        }
    }

    @SuppressWarnings("rawtypes")
    private void setExecutionStack(ExecutionStack stack) {
        try {

            StackFrame stackFrame = (StackFrame)stack.getTopFrame();
            XSuspendContext suspendContext = new SuspendContext(stack);

            XSourcePosition sourcePosition = stackFrame.getSourcePosition();

            XBreakpoint breakpoint = null;
            if (sourcePosition != null) {
                VirtualFile file = sourcePosition.getFile();

                breakpoint = breakpointHandler.getBreakpoint(
                        file.getName(),
                        sourcePosition.getLine()
                );
            }
            if (breakpoint != null) {
                getSession().breakpointReached(breakpoint, "", suspendContext);
            } else {
                getSession().positionReached(suspendContext);
            }

            ApplicationManager.getApplication().invokeLater(()->{
                getSession().setCurrentStackFrame(stack, stackFrame);
                getSession().showExecutionPoint();
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public XBreakpointHandler<?> @NotNull [] getBreakpointHandlers() {
        return new XBreakpointHandler[]{breakpointHandler};
    }

    @Override
    public void processConnected() {
        getSession().setPauseActionSupported(true);
    }

    @Override
    public void sessionInitialized() {
        super.sessionInitialized();
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        String threadName = context.getActiveExecutionStack().getDisplayName();
        debugServer.sendMessage(Message.stepInto(threadName));
    }

    public void sendMessage(Message message, Consumer<Message> callback){
        debugServer.sendMessage(message, callback);
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        String threadName = context.getActiveExecutionStack().getDisplayName();
        debugServer.sendMessage(Message.stepOver(threadName));
    }


    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        String threadName = context.getActiveExecutionStack().getDisplayName();
        debugServer.sendMessage(Message.stepReturn(threadName));
    }

    @Override
    public void stop() {
        debugServer.sendMessage(Message.threadKill(null));
        debugServer.close();
    }

    @Override
    public void startPausing() {
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        debugServer.sendMessage(Message.threadRun(null));
    }

    @Override
    public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
        return new XDebuggerEditorsProviderBase() {
            @Override
            protected PsiFile createExpressionCodeFragment(
                    @NotNull Project project1,
                    @NotNull String text,
                    @Nullable PsiElement context,
                    boolean isPhysical) {
                return null;
            }

            @Override
            public @NotNull FileType getFileType() {
                return FileTypes.PLAIN_TEXT;
            }
        };
    }

    @Override
    protected @Nullable ProcessHandler doGetProcessHandler() {
        return processHandler;
    }
}
