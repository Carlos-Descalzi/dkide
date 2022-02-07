package io.datakitchen.ide.editors.diff.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

import java.util.List;
import java.util.Map;

public class DiffCalculator {

    private final String left;
    private final String right;

    public DiffCalculator(String left, String right){
        this.left = left;
        this.right = right;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDifference() throws Exception{
        ObjectMapper jackson = new ObjectMapper();
        JsonNode beforeNode = jackson.readTree(left);
        JsonNode afterNode = jackson.readTree(right);
        JsonNode patchNode = JsonDiff.asJson(beforeNode, afterNode);
        return jackson.readValue(patchNode.toString(), List.class);
    }

}
