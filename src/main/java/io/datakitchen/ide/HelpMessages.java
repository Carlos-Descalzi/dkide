package io.datakitchen.ide;

public interface HelpMessages {

    String SITE_HELP_MSG = "To add more sites, add to DataKitchen menu -> Sites";
    String ACCOUNT_HELP_MSG = "To set up accounts, go to DataKitchen menu -> "
            +"Edit Global Configuration/Edit Project Configuration, on Accounts tab";
    String IMPORT_SITES_HELP_MSG = "This option will import only those URLs which are not "
            +"any of the default sites for the application";

    String KITCHEN_PATH_MESSAGE = "It must be a kitchen folder previously pulled from dk command line.";
    String RECIPE_NAME_MESSAGE = "Ensure to use the appropiate context for creating the recipe on the selected kitchen path";
    String CONNECTOR_MSG = "Connectors are specified in overrides and/or recipe variables."+
            "If the field doesn't show any option, create a connector in either place";
    String CONNECTION_MSG = "By using this option, the connector will be created with blank configuration."
            + "To make it work, input settings will need to be filled on the connection.";
    String CONFIG_KITCHEN_NAME_MSG = "Specifies the kitchen name to be used when running the recipe, it is set into runtime "+
            "variables as \"CurrentKitchen\"";
    String CONFIG_INGREDIENT_SITE = "Specifies the site where to invoke when running ingredients";
    String CONFIG_SIMPLIFIED_VIEW_MSG = "Enabling this option will present simplified views for nodes, including on the recipe "+
            "tree. Disabling it will show files for each artifact in nodes and a form specific for each one";
    String HIDE_NODES_MSG = "Hides nodes which don't belong to the active variation, this option can be toggled by pressing CTRL+SHIFT+ALT+H in project tree.";
    String CUSTOM_FORMS_MSG = "Disabling this option will disable forms for all files, and files will be edited using regular text editor";
    String CUSTOM_NODE_FORMS = "Enables/disables forms only for nodes, leaving active forms for variations, description, etc.";
    String CUSTOM_DS_FORMS = "Enables/disables forms only for data sources and data sinks";
    String CONFIG_JSON_MSG = "When using Script Node, the form will edit both notebook and config.json, this option allos to see the file in project tree";
}
