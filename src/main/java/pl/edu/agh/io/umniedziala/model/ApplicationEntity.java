package pl.edu.agh.io.umniedziala.model;

import pl.edu.agh.io.umniedziala.databaseUtilities.QuerryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ApplicationEntity {

    public static final String TABLE_NAME = "application";

    protected int id;
    protected String name;
    protected String applicationPath;
    protected String color;

    private ApplicationEntity(final int id, final String name, final String applicationPath, final String color) {
        this.id = id;
        this.name = name;
        this.applicationPath = applicationPath;
        this.color = color;
    }

    public static Optional<ApplicationEntity> create(final String name, final String applicationPath, final String color) {
        String insertSql = String.format(
                "INSERT INTO %s (%s, %s, %s) VALUES ('%s', '%s', '%s')"
                , TABLE_NAME, Columns.NAME, Columns.APPLICATION_PATH, Columns.COLOR
                , name, applicationPath, color
        );

        int id = 0;

        try {
            id = QuerryExecutor.createAndObtainId(insertSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ApplicationEntity.findById(id);
    }

    public static void updateApplicationColor(final String name, final String color){

        String updateSql = String.format(
                "UPDATE %s SET %s = '%s' WHERE %s = '%s'",
                TABLE_NAME, Columns.COLOR, color, Columns.NAME, name
        );

        try {
            QuerryExecutor.update(updateSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Optional<ApplicationEntity> findByName(final String name) {
        String findByNameSql = String.format("SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, Columns.NAME, name);

        try {
            ResultSet rs = QuerryExecutor.read(findByNameSql);
            return returnApplication(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static Optional<ApplicationEntity> findByApplicationPath(final String applicationPath) {
        String findByNameSql = String.format("SELECT * FROM %s WHERE %s = '%s'", TABLE_NAME, Columns.APPLICATION_PATH, applicationPath);

        ResultSet rs;
        try {
            rs = QuerryExecutor.read(findByNameSql);
            return returnApplication(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static Map<String, String> getApplicationsColors(){
        String getApplicationsColorsSQL = String.format("SELECT name, color FROM %s",TABLE_NAME);
        Map<String, String> results = new HashMap<>();
        try {
            ResultSet rs = QuerryExecutor.read(getApplicationsColorsSQL);
            while (rs.next()){
                results.put(rs.getString(Columns.NAME), rs.getString(Columns.COLOR));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }


    public static Optional<ApplicationEntity> findById(final int id) {
        String findByIdSql = String.format("SELECT * FROM %s WHERE %s = %d", TABLE_NAME, Columns.ID, id);
        try {
            ResultSet rs = QuerryExecutor.read(findByIdSql);
            return returnApplication(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static List<ApplicationEntity> getAllApplications() {
        String getAllApplicationsSql = String.format("SELECT * FROM " + TABLE_NAME);

        List<ApplicationEntity> resultList = new ArrayList<>();
        try {
            ResultSet rs = QuerryExecutor.read(getAllApplicationsSql);
            while (rs.next()) {
                resultList.add(new ApplicationEntity(
                        rs.getInt(Columns.ID)
                        , rs.getString(Columns.NAME)
                        , rs.getString(Columns.APPLICATION_PATH)
                        , rs.getString(Columns.COLOR)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }


    private static Optional<ApplicationEntity> returnApplication(ResultSet rs) {

        try {
            if (rs.isClosed())
                return Optional.empty();

            return Optional.of(new ApplicationEntity(
                    rs.getInt(Columns.ID),
                    rs.getString(Columns.NAME),
                    rs.getString(Columns.APPLICATION_PATH),
                    rs.getString(Columns.COLOR)
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static void delete(final int id) throws SQLException {
        String deleteDis = String.format("delete from %s where %s = %s",
                TABLE_NAME,
                Columns.ID, id
        );
        QuerryExecutor.delete(deleteDis);
    }


    public List<RunningPeriodEntity> getRunningPeriodEntitiesList() {
        String findRunningPeriodEntitiesSql = String.format(
                "SELECT * " +
                "FROM running_period rp " +
                "JOIN application app ON app.id = rp.application_id " +
                "WHERE app.id = %d "
                , this.id
        );

        List<RunningPeriodEntity> resultList = new LinkedList<>();
        try {
            ResultSet rs = QuerryExecutor.read(findRunningPeriodEntitiesSql);

            while (rs.next()) {
                resultList.add(new RunningPeriodEntity(
                        rs.getInt(RunningPeriodEntity.Columns.ID)
                        , rs.getString(RunningPeriodEntity.Columns.START_TIME)
                        , rs.getString(RunningPeriodEntity.Columns.END_TIME)
                        , rs.getInt(RunningPeriodEntity.Columns.APPLICATION_ID)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApplicationPath() {
        return applicationPath;
    }

    public String getColor() {
        return color;
    }


    public static class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String APPLICATION_PATH = "application_path";
        public static final String COLOR = "color";
    }
}
