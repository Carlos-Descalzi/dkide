package io.datakitchen.ide;

import java.io.File;

public class Constants {
    public static final String USER_CONFIG_FOLDER_NAME = ".dkide";

    public static final File USER_CONFIG_FOLDER = new File(System.getProperty("user.home"),USER_CONFIG_FOLDER_NAME);

    public static final File LIBRARY_FILE = new File(USER_CONFIG_FOLDER, "library.json");
    public static final String RECIPE_RUNNER_IMAGE = "datakitchenprivate/dk_recipe_runner:latest-sd9987";
    public static final String GPC_IMAGE = "datakitchenprod/dk_general_purpose_container:latest";
    public static final String GPC_DEBUG_IMAGE = "datakitchenprivate/dk_general_purpose_container:latest-debug";
    public static final String GPC_WORK_DIR = "/dk/AnalyticContainer/DKGeneralPurposeContainer";
    public static final String DOCKER_SHARE_FOLDER_NAME = "docker-share";
    public static final String DOCKER_SHARE_FOLDER = GPC_WORK_DIR+ "/"+DOCKER_SHARE_FOLDER_NAME;
    public static final String PYTHON_PLUGIN_ID = "PythonCore";
    public static final String DOCKERHUB_IMAGE_URL_PREFIX = "https://hub.docker.com/";
    public static final String SCHEMA_PREFIX = "https://datakitchen.io/schemas/";

    public static final String FILE_DESCRIPTION_JSON = "description.json";
    public static final String FILE_VARIATIONS_JSON = "variations.json";
    public static final String FILE_VARIABLES_JSON = "variables.json";
    public static final String FILE_NOTEBOOK_JSON = "notebook.json";
    public static final String FILE_README_MD = "README.md";

    public static final String FOLDER_ACTIONS = "actions";
    public static final String FOLDER_DATA_SOURCES = "data_sources";
    public static final String FOLDER_DATA_SINKS = "data_sinks";
    public static final String FOLDER_RESOURCES = "resources";

    public static final String NOTIFICATION_GROUP_ID = "io.datakitchen.ide";

//    public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.create(
//            "io.datakitchen.ide",
//            NotificationDisplayType.BALLOON,
//            true,
//            "io.datakitchen.notifications",
//            null,
//            null,
//            null);

}