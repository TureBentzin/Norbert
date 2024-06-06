package de.bentzin.norbert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class GsonManager {
    @NotNull
    public static final Logger logger = LoggerFactory.getLogger(GsonManager.class);
    @NotNull
    private final Gson gson;


    public GsonManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @NotNull
    public Gson getGson() {
        return gson;
    }

    @Nullable
    public <T> T fromJson(@NotNull File file, @NotNull Class<T> classOfT) {
        if (file.exists()) {
            if (!file.getName().endsWith(".json")) {
                logger.warn("File {} does not end with .json!", file.getAbsolutePath());
            }
            try {
                return gson.fromJson(new java.io.FileReader(file), classOfT);
            } catch (FileNotFoundException e) {
                logger.error("Could not read file {}!", file.getAbsolutePath());
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (JsonSyntaxException e) {
                logger.error("Could not parse file {}!", file.getAbsolutePath());
                logger.error(e.getMessage(), e);
                return null;
            } catch (JsonIOException e) {
                logger.error("Gson failed fatally trying to read file {}!", file.getAbsolutePath());
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public void toJson(@NotNull File file, @NotNull Object object) {

        try (FileWriter fileWriter = new FileWriter(file);) {
            gson.toJson(object, fileWriter);
            if (de.bentzin.norbert.Bot.debug) {
                String json_copy = gson.toJson(object);
                logger.info("Json: {}", json_copy);
            }
        } catch (JsonIOException e) {
            logger.error("Gson failed fatally trying to write file {}!", file.getAbsolutePath());
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.error("Could not write file {}!", file.getAbsolutePath());
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
