package net.kettlemc.language.mysql;

import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.entity.LanguageEntity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SQLHandler {

    public static void save(LanguageEntity entity) {
        LanguageAPI.getSqlProvider().prepareReplace(LanguageEntity.class).object(entity).queue();
    }

    public static LanguageEntity load(String uuid) {
        Future<LanguageEntity> future = LanguageAPI.getSqlProvider().preparePrimarySelect(LanguageEntity.class).values(uuid).queue();
        LanguageEntity loadedEntity = null;
        try {
            loadedEntity = future.get(30l, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LanguageAPI.LOGGER.error("Problem loading entity for uuid " + uuid + ". See error below.");
            e.printStackTrace();
        }
        if (loadedEntity == null) {
            loadedEntity = new LanguageEntity(uuid);
            save(loadedEntity);
        }
        return loadedEntity;
    }

}
