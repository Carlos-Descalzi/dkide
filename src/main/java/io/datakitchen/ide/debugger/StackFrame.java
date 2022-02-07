package io.datakitchen.ide.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.*;
import org.apache.commons.codec.net.URLCodec;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackFrame extends XStackFrame {

    private final List<VirtualFile> sourceFolders;
    private final String containerBasePath;
    private final long id;
    private final String filename;
    private final int line;
    private final Map<String, Object> locals;
    private final Map<String, Object> globals;
    private final SourcePosition sourcePosition;
    private ExecutionStack stack;
    private final PythonDebugProcess debugProcess;

    public boolean equals(Object other){
        return other instanceof StackFrame
                && id == ((StackFrame)other).id;
    }

    public int hashCode(){
        return Long.valueOf(id).hashCode();
    }

    public StackFrame(
            PythonDebugProcess debugProcess,
            List<VirtualFile> sourceFolders,
            String containerBasePath,
            long id,
            String filename,
            int line,
            Map<String, Object> locals,
            Map<String, Object> globals){
        this.debugProcess = debugProcess;
        this.id = id;
        this.containerBasePath = containerBasePath;
        this.sourceFolders = sourceFolders;
        this.filename = filename;
        this.line = line;
        this.locals = locals;
        this.globals = globals;
        this.sourcePosition = SourcePosition.create(sourceFolders, containerBasePath, filename, line);
        if (this.sourcePosition == null){
            throw new RuntimeException("Invalid frame"); // TODO PUT A BETTER EXCEPTION HERE
        }
    }

    public ExecutionStack getStack() {
        return stack;
    }

    public void setStack(ExecutionStack stack) {
        this.stack = stack;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {

        debugProcess.sendMessage(Message.getFrame(stack.getThreadId(), this.id),(Message response)->{
            try {
                XValueChildrenList list = parseValues(response);
                node.addChildren(list, true);

            }catch(Exception ex){
                ex.printStackTrace();
            }

        });

    }

    public XSourcePosition getSourcePosition() {
        return this.sourcePosition;
    }

    @NotNull
    private XValueChildrenList parseValues(Message response) throws SAXException, IOException, ParserConfigurationException {
        XValueChildrenList list = new XValueChildrenList();

        Node rootNode = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(response.getPayload())))
                .getDocumentElement();

        NodeList children = rootNode.getChildNodes();

        URLCodec codec = new URLCodec();


        for (int i=0;i<children.getLength();i++){
            Node varNode = children.item(i);

            try {
                String name = varNode.getAttributes().getNamedItem("name").getTextContent();
                String type = varNode.getAttributes().getNamedItem("type").getTextContent();
                String valueEncoded = varNode.getAttributes().getNamedItem("value").getTextContent();
                String fullValue = codec.decode(codec.decode(valueEncoded)); // double encoded ????
                String value = fullValue.substring(fullValue.indexOf(":")+1);

                list.add(name, new XValue() {
                    @Override
                    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
                        node.setPresentation(AllIcons.Debugger.Db_primitive, type, value, false);
                    }
                });
            }catch(Exception ignored){}
        }
        return list;
    }

    public static StackFrame fromXml(PythonDebugProcess debugProcess, List<VirtualFile> sourceFolders, String containerBasePath, Node xmlNode){

        long id = Long.parseLong(xmlNode.getAttributes().getNamedItem("id").getTextContent());
        String file = xmlNode.getAttributes().getNamedItem("file").getTextContent();
        int line = Integer.parseInt(xmlNode.getAttributes().getNamedItem("line").getTextContent());

        return new StackFrame(debugProcess, sourceFolders, containerBasePath, id, file, line-1, new HashMap<>(), new HashMap<>());

    }
}
