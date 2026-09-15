package com.naikeri.sgw.impl.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.naikeri.sgw.helpers.SgwResource;
import com.naikeri.sgw.impl.db.entity.Realm;
import com.naikeri.sgw.impl.db.repository.RealmRepository;
import com.naikeri.sgw.impl.db.persistence.Column;
import com.naikeri.sgw.impl.db.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class DataSource implements RealmRepository {

    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);


    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final Template template;
    private Connection connection;
    private static DataSource instance = null;

    private static boolean initialized = false;

    private static synchronized DataSource getInstance() {
        if (!initialized) {
            initialized = true;
            try {
                instance = new DataSource();
                logger.info("DataSource connected to '{}'", instance.template.getUrl());
            } catch (Exception e) {
                logger.warn("No database available, so realms are taken from the configuration file only "
                        + "and newly seen realms are not persisted", e);
            }
        }
        return instance;
    }

    public DataSource() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        this.template = mapper.readValue(new SgwResource("application.yaml").getAsStream(), Template.class);
        this.connection = connect();
    }

    private Connection connect() throws SQLException {
        Properties properties = new Properties();
        properties.put("user", template.getUser());
        properties.put("password", template.getPassword());
        return DriverManager.getConnection(template.getUrl(), properties);
    }

    /**
     * The connection is opened once and kept, so a database restart or an idle timeout would otherwise leave
     * this agent permanently disconnected for the rest of its run. Every statement goes through here, which
     * validates the connection and reopens it when the server has gone away.
     */
    private synchronized Connection connection() throws SQLException {
        try {
            if (connection != null && connection.isValid(VALIDATION_TIMEOUT_SECONDS)) {
                return connection;
            }
        }
        catch (SQLException e) {
            logger.debug("Connection validation failed, reconnecting", e);
        }
        closeQuietly(connection);
        logger.info("Reconnecting to '{}'", template.getUrl());
        connection = connect();
        return connection;
    }

    private static void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                logger.debug("Failed to close the previous connection", e);
            }
        }
    }

    /**
     * Returns the repository, or null when no database is reachable: the agent runs without one, routing from
     * naikeri-signaling-gateway.xml alone, so every caller must handle null rather than assume a repository.
     */
    public static DataSource initialize() {
        logger.info("DataSource is initializing...");
        return DataSource.getInstance();
    }

    /**
     * Runs a query whose placeholders are JDBC '?' markers, binding params in order. Values reach here from
     * peer messages (a realm name in a CER, for instance), so they are never formatted into the statement.
     */
    public <T> List<T> findByQuery(Class<T> classEntity, String query, String... params) {

        try (PreparedStatement st = connection().prepareStatement(query)) {
            Field[] fields = classEntity.getDeclaredFields();
            for (int i = 0; i < params.length; i++) {
                st.setString(i + 1, params[i]);
            }

            List<T> result = new ArrayList<>();
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    T entity = classEntity.getDeclaredConstructor().newInstance();
                    setValues(entity, fields, rs);
                    result.add(entity);
                }
            }

            return result;
        } catch (Exception e) {
            logger.warn("Exception caught running query [{}]", query, e);
            return null;
        }
    }

    private void setValues(Object entity, Field[] fields, ResultSet rs) throws Exception {
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class) && field.getType().getPackage().getName().equals("java.lang")) {

                field.set(entity, rs.getObject(field.getAnnotation(Column.class).name()));
            } else if (field.getType().isArray() || field.getType().getPackage().getName().equals("java.lang")) {
                Object value = rs.getObject(field.getName());
                if (value instanceof org.postgresql.jdbc.PgArray) {
                    //TODO: add other type of array
                    field.set(entity, ((org.postgresql.jdbc.PgArray) value).getArray());
                } else {
                    field.set(entity, rs.getObject(field.getName()));
                }
            } else if (!field.getType().getClass().equals(List.class)) {
                Object subEntity = field.getType().getDeclaredConstructor().newInstance();
                Field[] subFields = subEntity.getClass().getDeclaredFields();
                setValues(subEntity, subFields, rs);
                field.set(entity, subEntity);
            }

            field.setAccessible(false);
        }
    }

    public <T> Long save(T t) {
        try {
            Table persistence = t.getClass().getAnnotation(Table.class);
            Field[] fields = t.getClass().getDeclaredFields();
            List<String> keys = new ArrayList<>();
            List<Object> vls = new ArrayList<>();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                if (field.isAnnotationPresent(Column.class) && field.get(t) != null) {
                    keys.add(field.getAnnotation(Column.class).name());
                    vls.add(field.get(t));
                } else if ((field.getType().isArray() || field.getType().getPackage().getName().equals("java.lang")) && field.get(t) != null) {
                    keys.add(field.getName());
                    vls.add(field.get(t));
                }
                field.setAccessible(false);
            }
            String sql = "INSERT INTO " + persistence.name() + "(" + String.join(",", keys) + ") " +
                    "VALUES (?" + String.join(",?", keys.stream().map(f -> "").collect(Collectors.toList())) + ")";
            Connection conn = connection();
            PreparedStatement pStatement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            for (int j = 0; j < vls.size(); j++) {
                Object obj = vls.get(j);
                if (obj instanceof Long) {
                    pStatement.setLong(j + 1, (Long) obj);
                } else if (obj instanceof String) {
                    pStatement.setString(j + 1, obj.toString());
                } else if (obj instanceof String[]) {
                    pStatement.setArray(j + 1, conn.createArrayOf("varchar", (Object[]) obj));
                } else if (obj instanceof Boolean) {
                    pStatement.setBoolean(j + 1, (Boolean) obj);
                } else if (obj instanceof Integer) {
                    pStatement.setInt(j + 1, (int) obj);
                } else if (obj instanceof Double) {
                    pStatement.setDouble(j + 1, (Double) obj);
                } else if (obj instanceof Float) {
                    pStatement.setFloat(j + 1, (Float) obj);
                }
            }

            if (pStatement.executeUpdate() > 0) {
                ResultSet resultSet = pStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                resultSet.close();
            }
            pStatement.close();
        } catch (Exception e) {
            logger.warn("Exception caught", e);
        }
        return null;
    }

    @Override
    public void saveRealm(Realm realm) {

        Long realmId = save(realm);
        Long applId = null;
        try {
            PreparedStatement pStatement = connection().prepareStatement("SELECT appl_id FROM application_id WHERE vendor_id = ? and auth_appl_id = ? and acct_appl_id = ?;");
            pStatement.setLong(1, realm.getApplicationId().getVendorId());
            pStatement.setLong(2, realm.getApplicationId().getAuthApplId());
            pStatement.setLong(3, realm.getApplicationId().getAcctApplId());
            ResultSet resultSet = pStatement.executeQuery();
            if (resultSet.next()) {
                applId = resultSet.getLong(1);
            }
            resultSet.close();
            pStatement.close();
        } catch (Exception e) {
            logger.warn("Exception caught", e);
        }
        if (applId == null) {
            applId = save(realm.getApplicationId());
        }
        linkRealmApplication(realmId, applId);
    }

    private void linkRealmApplication(Long realmId, Long applId) {
        if (realmId == null || applId == null) {
            logger.warn("Not linking realm '{}' to application '{}': one of them was not saved", realmId, applId);
            return;
        }
        try (PreparedStatement pStatement =
                     connection().prepareStatement("INSERT INTO realm_application(realm_id, appl_id) VALUES (?, ?)")) {
            pStatement.setLong(1, realmId);
            pStatement.setLong(2, applId);
            pStatement.executeUpdate();
        } catch (Exception e) {
            logger.warn("Exception caught linking realm '{}' to application '{}'", realmId, applId, e);
        }
    }

    @Override
    public List<Realm> findAll() {
        return findByQuery(Realm.class, "SELECT r.*, a.* " +
                "FROM realm r JOIN realm_application ra " +
                "ON r.realm_id = ra.realm_id JOIN application_id a ON a.appl_id = ra.appl_id;");
    }

}
