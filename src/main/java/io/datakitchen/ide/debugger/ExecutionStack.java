package io.datakitchen.ide.debugger;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class ExecutionStack extends XExecutionStack {

    private final List<StackFrame> frames;

    public ExecutionStack(String threadId, List<StackFrame> frames){
        super(threadId);
        this.frames = frames;
        for (StackFrame frame: frames){
            frame.setStack(this);
        }
    }

    public String getThreadId(){
        return getDisplayName();
    }

    @Override
    public @Nullable XStackFrame getTopFrame() {
        return frames.isEmpty() ? null : frames.get(0);
    }

    @Override
    public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
        container.addStackFrames(frames.subList(firstFrameIndex,frames.size()),true);
    }

    public static ExecutionStack fromXml(
            PythonDebugProcess debugProcess,
            List<VirtualFile> sourceFolders,
            String containerBasePath,
            Node xmlNode
    ){
        String threadId = xmlNode.getAttributes().getNamedItem("id").getTextContent();

        List<StackFrame> stackFrames = new ArrayList<>();
        NodeList nodeList = xmlNode.getChildNodes();
        for (int i=0; i<nodeList.getLength();i++){
            try {
                stackFrames.add(StackFrame.fromXml(debugProcess, sourceFolders, containerBasePath, nodeList.item(i)));
            }catch(Exception ex){
                // invalid frame, doesn't belong to the code of interest.
            }
        }

        return new ExecutionStack(threadId, stackFrames);
    }
}
