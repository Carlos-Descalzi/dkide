package io.datakitchen.ide.ui;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.service.CustomIconService;
import io.datakitchen.ide.util.JsonUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Objects;

public class UIUtil {

    public static final Border EMPTY_BORDER_5x5 = JBUI.Borders.empty(5);
    public static final Border EMPTY_BORDER_10x10 = JBUI.Borders.empty(10);

    public static Cursor toCursor(JComponent component) {
        Dimension size = component.getSize();
        BufferedImage image = com.intellij.util.ui.UIUtil.createImage(component,size.width,size.height,BufferedImage.TYPE_INT_ARGB);
        component.paint(image.getGraphics());
        return component.getToolkit().createCustomCursor(image, new Point(0,0),component.getName());
    }

    public static Notification showNotificationSync(Project project, String title, String message){
        Notification msg = NotificationGroupManager.getInstance()
                .getNotificationGroup(Constants.NOTIFICATION_GROUP_ID)
                .createNotification(title, message, NotificationType.INFORMATION);

        msg.notify(project);
//        Notification msg = new Notification(
//                Constants.NOTIFICATION_GROUP.getDisplayId(),
//                title,
//                message,
//                NotificationType.INFORMATION
//        );
//        Notifications.Bus.notify(msg, project);
////        msg.notify(project);
        return msg;
    }

    public static void showNotification(Project project, String title, String message){
        ApplicationManager.getApplication().invokeLater(()->{
            showNotificationSync(project, title, message);
        });
    }

    public static final Dimension PROJECT_TREE_ICON_SIZE = new Dimension(13,13);

    public static Icon getNodeIcon(Project project, VirtualFile nodeFolder, boolean active, Dimension dimension){

        Icon icon = getCustomIcon(project, nodeFolder, dimension);

        String iconName = getIconName(nodeFolder);
        if (icon == null && iconName != null) {
            icon = IconLoader.getIcon("/icons/" + iconName + (active ? "" : "_disabled") + ".svg", UIUtil.class);
        }

        return icon;
    }

    @Nullable
    public static Icon getCustomIcon(Project project, VirtualFile nodeFolder, Dimension dimension) {
        VirtualFile iconFile = nodeFolder.findChild("icon.svg");

        Icon icon = null;
        if (iconFile != null){
            icon = CustomIconService.getInstance(project).getIcon(iconFile, dimension);
        }
        return icon;
    }

    private static String getIconName(VirtualFile nodeFolder) {
        try {
            Map<String, Object> json = JsonUtil.read(Objects.requireNonNull(nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON)));

            return (String) json.get("type");
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }


}
