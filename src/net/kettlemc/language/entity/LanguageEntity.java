package net.kettlemc.language.entity;

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.SqlSerializable;
import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.mysql.SQLHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SqlSerializable("LanguageEntity")
public class LanguageEntity {

    private static List<LanguageEntity> entities = new ArrayList<>();

    @Column(value = "uuid", size = 40, primary = true)
    private final String uuid;
    @Column(value = "language", size = 2)
    private String language;

    public LanguageEntity(String uuid) {
        this.uuid = uuid;
        this.language = LanguageAPI.getDefaultLang().toLanguageTag();
        if (!entities.contains(this))
            entities.add(this);
    }

    public LanguageEntity() {
        this.uuid = null;
        this.language = LanguageAPI.getDefaultLang().toLanguageTag();
        if (!entities.contains(this))
            entities.add(this);
    }

    public String getUUID() {
        return this.uuid;
    }

    public Locale getLanguage() {
        return Locale.forLanguageTag(this.language);
    }

    public void setLanguage(Locale language) {
        this.language = language.toLanguageTag();
    }

    public static List<LanguageEntity> getEntities() {
        return entities;
    }

    public static boolean exists(String uuid) {
        return entities.contains(getEntity(uuid));
    }

    public static LanguageEntity getEntity(String uuid) {
        for (LanguageEntity entity : getEntities())
            if (entity.getUUID().equals(uuid))
                return entity;
        return new LanguageEntity(uuid);
    }

    public static void createPlayer(String uuid) {
        new LanguageEntity(uuid);
    }

    public void saveStats() {
        SQLHandler.save(this);
    }

}


