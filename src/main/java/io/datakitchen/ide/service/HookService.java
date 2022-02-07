package io.datakitchen.ide.service;

import com.intellij.openapi.project.Project;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.hooks.Action;
import io.datakitchen.ide.hooks.ActionHook;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HookService {

    public static HookService getInstance(Project project){
        return project.getService(HookService.class);
    }

    private final Project project;
    private final Map<String, ActionHook> hooks = new LinkedHashMap<>();
    private final List<Action> actions = new ArrayList<>();

    public HookService(Project project){
        this.project = project;
        loadHooks();
    }

    public ActionHook getHookForAction(String actionId){
        return hooks.get(actionId);
    }


    public List<Action> getActions(){
        return actions;
    }

    private void loadHooks(){

        File scriptsFolder = new File(Constants.USER_CONFIG_FOLDER.toString(),"scripts");

        System.out.println("Scripts folder:"+scriptsFolder.getAbsolutePath());
        if (scriptsFolder.exists()){
            try {
                GroovyScriptEngine engine = new GroovyScriptEngine(scriptsFolder.toURI().toString(), getClass().getClassLoader());

                for (File groovyFile : scriptsFolder.listFiles(pathname -> pathname.getName().endsWith(".groovy"))) {
                    System.out.println("Script:"+groovyFile.getAbsolutePath());
                    Object result = engine.run(groovyFile.getAbsolutePath(), new Binding());

                    if (result instanceof ActionHook) {
                        System.out.println("Action hook: " + result);
                        ActionHook actionHook = (ActionHook) result;
                        for (String action : actionHook.getActions()) {
                            hooks.put(action, actionHook);
                        }
                    } else if (result instanceof Action) {
                        actions.add((Action) result);
                    }
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

}
