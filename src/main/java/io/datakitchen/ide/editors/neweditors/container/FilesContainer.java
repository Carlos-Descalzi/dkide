package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.ContainerModel;
import io.datakitchen.ide.model.ContainerModelEvent;
import io.datakitchen.ide.model.ContainerModelListener;
import io.datakitchen.ide.ui.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilesContainer extends JPanel implements ContainerModelListener, DropTargetListener, DragGestureListener {

    private final JPanel filesHolder = new JPanel(new VerticalStackLayout());
    private final DragSource dragSource = new DragSource();
    private final ContainerModel model;
    private Predicate<VirtualFile> fileFilter;
    private Function<VirtualFile, List<Action>> actionSupplier;
    private boolean testFiles;

    public FilesContainer(ContainerModel model, boolean testFiles){
        this.model = model;
        this.testFiles = testFiles;
        setLayout(new BorderLayout());
        add(createLabel(testFiles), BorderLayout.NORTH);
        add(filesHolder, BorderLayout.CENTER);
        JButton addFilesButton = new JButton(new SimpleAction("+ Add files", this::addFiles));
        Font font = getFont();
        addFilesButton.setFont(font.deriveFont(font.getSize()-2f));
        addFilesButton.setContentAreaFilled(false);
        addFilesButton.setBorderPainted(false);
        addFilesButton.setHorizontalAlignment(SwingConstants.LEFT);
        addFilesButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filesHolder.add(addFilesButton);
        JButton createFileButton = new JButton(new SimpleAction("+ Create new file", this::createNewFile));
        createFileButton.setFont(font.deriveFont(font.getSize()-2f));
        createFileButton.setContentAreaFilled(false);
        createFileButton.setBorderPainted(false);
        createFileButton.setHorizontalAlignment(SwingConstants.LEFT);
        createFileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        filesHolder.add(createFileButton);
        this.model.addContainerModelListener(this);
        loadFiles();
        DropTarget dropTarget = new DropTarget();
        try {
            dropTarget.addDropTargetListener(this);
        }catch(TooManyListenersException ignored){}
        dropTarget.setActive(true);
        filesHolder.setDropTarget(dropTarget);
    }

    private JComponent createLabel(boolean testFiles) {
        if (testFiles){
            return new HelpContainer(
                new JLabel("Test Files", SwingConstants.RIGHT),
                "Place here any input file which is only used for testing the scripts"
            );
        }
        return new HelpContainer(
                new JLabel("Files", SwingConstants.RIGHT),
                "Place here any input file which will be copied to the container before starts executing"
        );

    }

    public Function<VirtualFile, List<Action>> getActionSupplier() {
        return actionSupplier;
    }

    public void setActionSupplier(Function<VirtualFile, List<Action>> actionSupplier) {
        this.actionSupplier = actionSupplier;
    }

    public Predicate<VirtualFile> getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(Predicate<VirtualFile> fileFilter) {
        this.fileFilter = fileFilter;
        loadFiles();
    }

    private void loadFiles(){
        for (InputFileView v:getFileViews()){
            filesHolder.remove(v);
        }
        Set<VirtualFile> files = doGetFiles();

        if (fileFilter != null){
            files = files.stream().filter(fileFilter).collect(Collectors.toSet());
        }

        for (VirtualFile file: files){
            addFile(file);
        }
        validate();
        repaint();
    }

    private void addFile(VirtualFile file) {
        InputFileView fileView = new InputFileView(model.getModule(), file, this::getFileActions);
        filesHolder.add(fileView, filesHolder.getComponentCount()-2);
        dragSource.createDefaultDragGestureRecognizer(fileView,DnDConstants.ACTION_COPY,this);
    }

    private Action[] getFileActions(LabelWithActions labelWithActions) {
        InputFileView fileView = (InputFileView) labelWithActions;
        VirtualFile file = fileView.getFile();

        List<Action> actions = new ArrayList<>();
        if (actionSupplier != null){
            actions.addAll(actionSupplier.apply(file));
        }
        actions.add(new SimpleAction("Rename", e -> renameFile(labelWithActions, file)));
        actions.add(new SimpleAction("Remove", e -> removeFile(file)));

        return actions.toArray(Action[]::new);
    }

    private void renameFile(LabelWithActions labelWithActions, VirtualFile file) {
        TextFieldInlineEditor editor = new TextFieldInlineEditor(file.getName());

        InlineEditorPopup.edit(labelWithActions, editor, newName -> {
            if (StringUtils.isNotBlank(newName)){
                model.renameFile(file, newName);
            }
        });
    }

    private void removeFile(VirtualFile file) {
        doRemoveFile(file);
    }


    private List<InputFileView> getFileViews(){
        return Arrays.stream(filesHolder.getComponents())
                .filter(c -> c instanceof InputFileView)
                .map(c -> (InputFileView)c).collect(Collectors.toList());
    }

    @Override
    public void inputFilesAdded(ContainerModelEvent event) {
        if (event.isTestFile() == testFiles) {
            for (VirtualFile file : event.getFiles()) {
                addFile(file);
            }
            if (event.isNewFile()) {
                FileEditorManager.getInstance(model.getProject()).openFile(event.getFiles().get(0), true);
            }
            validate();
        }
    }

    @Override
    public void inputFilesRemoved(ContainerModelEvent event) {
        if (event.isTestFile() == testFiles) {
            for (VirtualFile file : event.getFiles()) {
                for (InputFileView fileView : getFileViews()) {
                    if (file.equals(fileView.getFile())) {
                        filesHolder.remove(fileView);
                        break;
                    }
                }
            }
            validate();
        }
    }

    @Override
    public void inputFileRenamed(ContainerModelEvent event) {
        for (InputFileView fileView : getFileViews()){
            if (fileView.getFile().getName().equals(event.getOldName())) { // check by reference, not by equals!
                fileView.repaint();
            }
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {}

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);

            List<File> files = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            for (File file:files) {
                if (testFiles) {
                    doAddFile(file);
                }
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private Set<VirtualFile> doGetFiles(){
        return testFiles ? model.getTestFiles() : model.getFiles();
    }

    private void doRemoveFile(VirtualFile file){
        if (testFiles){
            model.removeTestFile(file);
        } else {
            model.removeFile(file);
        }
    }

    private void doAddFile(File file){
        if (testFiles){
            model.addTestFile(file);
        } else {
            model.addFile(file);
        }
    }

    private void doAddNewFile(String fileName){
        if (testFiles){
            model.addNewTestFile(fileName);
        } else {
            model.addNewFile(fileName);
        }
    }

    private void addFiles(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            doAddFile(chooser.getSelectedFile());
        }
    }

    private void createNewFile(ActionEvent event) {
        NewFileDialog dialog = new NewFileDialog(testFiles);
        if (dialog.showAndGet()){
            String fileName = dialog.getFileName();
            doAddNewFile(fileName);
        }
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        InputFileView fileView = (InputFileView) dge.getComponent();
        dge.startDrag(UIUtil.toCursor(fileView), new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.javaFileListFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DataFlavor.javaFileListFlavor);
            }

            @NotNull
            @Override
            public Object getTransferData(DataFlavor flavor) {
                List<File> fileList = new ArrayList<>();
                VirtualFile file = fileView.getFile();
                fileList.add(new File(file.getPath()));
                return fileList;
            }
        });
    }
}
