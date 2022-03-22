package net.kettlemc.language.entity;

import net.kettlemc.language.LanguageAPI;
import net.kettlemc.language.mysql.async.PreparedStatementExec;
import net.kettlemc.language.mysql.async.PreparedStatementQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageEntity {

    private static List<LanguageEntity> entities = new ArrayList<>();

    private String uuid;
    private Locale language;

    private int loadedState = 0;


    public LanguageEntity(String uuid) {
        this.uuid = uuid;
        this.language = LanguageAPI.getDefaultLang();
        if (!entities.contains(this))
            entities.add(this);
    }

    public String getUUID() {
        return this.uuid;
    }

    public Locale getLanguage() {
        return this.language;
    }

    public void setLanguage(Locale language) {
        this.language = language;
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

    public Object loadLanguage() {
        Object wait = new Object();
        LanguageAPI.getMySQLClient().getAsyncPreparedQuery("SELECT language FROM language WHERE language.uuid=?;", new PreparedStatementQuery() {

            @Override
            public void onStatementPrepared(PreparedStatement statement) {
                try {
                    statement.setString(1, uuid);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResultReceive(ResultSet result) {
                try {
                    if (!result.next())
                        loadedState = 1;
                    else {
                        language = Locale.forLanguageTag(result.getString("language"));
                        loadedState = 2;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                synchronized (wait) {
                    wait.notifyAll();
                }
            }
        });

        return wait;
    }

    public void saveStats() {
        String sql = loadedState == 1 ? "INSERT INTO language (uuid,language) VALUES (?,?);" : "UPDATE language SET language=? WHERE language.uuid=?;";
        LanguageAPI.getMySQLClient().getAsyncPreparedQuery(sql, new PreparedStatementExec() {

            @Override
            public void onStatementPrepared(PreparedStatement statement) {
                try {
                    if (loadedState != 1) {
                        statement.setString(1, language.toLanguageTag());
                        statement.setString(2, uuid);
                    } else {
                        statement.setString(1, uuid);
                        statement.setString(2, language.toLanguageTag());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatementExec(boolean exec) {
                loadedState = 2;
            }
        });
    }

    public boolean isLoaded() {
        return this.loadedState > 0;
    }
}


