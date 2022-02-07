package io.datakitchen.ide.editors;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.json.CustomJsonParser;
import io.datakitchen.ide.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DsInfo {
    private final String name;
    private final List<String> keys;

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public DsInfo(String name, List<String> keys) {
        this.name = name;
        this.keys = keys;
    }

    public List<String> getKeys() {
        return keys;
    }


    public static List<DsInfo> loadDsItems(VirtualFile nodeFolder, String folderName) {
        return loadDsItems(nodeFolder, folderName, false);
    }
    public static List<DsInfo> loadDsItems(VirtualFile nodeFolder, String folderName, boolean addWildcard) {
        List<DsInfo> dsItems = new ArrayList<>();
        VirtualFile dataSourcesFolder = nodeFolder.findChild(folderName);
        if (dataSourcesFolder != null){
            for (VirtualFile item: dataSourcesFolder.getChildren()){
                if (item.getName().endsWith(".json")) {
                    try {
                        Map<String,Object> dsObj = CustomJsonParser.parse(item);

                        Map<String, Object> dsKeys = ObjectUtil.cast(dsObj.get("keys"));

                        DsInfo dsInfo = new DsInfo(
                                        item.getName().replace(".json", ""),
                                        new ArrayList<>(dsKeys.keySet()));
                        if (addWildcard){
                            dsInfo.keys.add("*");
                        }
                        dsItems.add(dsInfo);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return dsItems;
    }
}
