package pl.edu.agh.io.umniedziala.model;

import pl.edu.agh.io.umniedziala.databaseUtilities.QuerryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackgroundPeriodEntity extends AppPeriod {

    public static final String TABLE_NAME = "background_period";

    protected int applicationId;

    public BackgroundPeriodEntity(final int id, final String startTime, final String endTime, final int applicationId) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.applicationId = applicationId;
    }

    public static Optional<BackgroundPeriodEntity> create(final String startTime, final String endTime, final int applicationId) {
        String insertSql = String.format(
                "INSERT INTO %s (%s, %s, %s) VALUES ('%s', '%s', %d)"
                , TABLE_NAME, Columns.START_TIME, Columns.END_TIME, Columns.APPLICATION_ID
                , startTime, endTime, applicationId
        );

        int id = 0;

        try {
            id = QuerryExecutor.createAndObtainId(insertSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BackgroundPeriodEntity.findById(id);
    }


    public static void update(final int id, final String startTime, final String endTime) {
        String updateSql = String.format(
                "UPDATE %s SET %s = '%s', %s = '%s' WHERE %s = %d"
                , TABLE_NAME
                , Columns.START_TIME, startTime
                , Columns.END_TIME, endTime
                , Columns.ID, id
        );

        try {
            QuerryExecutor.update(updateSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(final int id, final String endTime) {
        String updateSql = String.format(
                "UPDATE %s SET %s = '%s' WHERE %s = %d"
                , TABLE_NAME
                , Columns.END_TIME, endTime
                , Columns.ID, id
        );

        try {
            QuerryExecutor.update(updateSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Optional<BackgroundPeriodEntity> findById(final int id) {
        String findByIdSql = String.format("SELECT * FROM %s WHERE %s = %s", TABLE_NAME, Columns.ID, id);

        try {
            ResultSet rs = QuerryExecutor.read(findByIdSql);
            return returnBackgroundPeriod(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static List<BackgroundPeriodEntity> findByStartDate(final String startDate) {
        String findByStartDateSql = String.format(
                "SELECT * FROM %s " +
                        "INNER JOIN %s ON %s.%s = %s.%s " +
                        "WHERE %s >= Datetime('%s 00:00:00') and %s <= Datetime('%s 23:59:59') "
                , TABLE_NAME
                , ApplicationEntity.TABLE_NAME, ApplicationEntity.TABLE_NAME, ApplicationEntity.Columns.ID
                , TABLE_NAME, Columns.APPLICATION_ID
                , Columns.START_TIME, startDate
                , Columns.START_TIME, startDate
        );

        Optional<ResultSet> rs = Optional.empty();
        try {
            rs = Optional.of(QuerryExecutor.read(findByStartDateSql));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<BackgroundPeriodEntity> resutList = new ArrayList<>();
        if (rs.isPresent()) {
            try {
                while (rs.get().next()) {
                    resutList.add(new BackgroundPeriodEntity(
                            rs.get().getInt(Columns.ID),
                            rs.get().getString(Columns.START_TIME),
                            rs.get().getString(Columns.END_TIME),
                            rs.get().getInt(Columns.APPLICATION_ID)
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return resutList;
    }



    public static Optional<BackgroundPeriodEntity> returnBackgroundPeriod(ResultSet rs) {
        try {
            if (rs.isClosed())
                return Optional.empty();

            return Optional.of(new BackgroundPeriodEntity(
                    rs.getInt(Columns.ID),
                    rs.getString(Columns.START_TIME),
                    rs.getString(Columns.END_TIME),
                    rs.getInt(Columns.APPLICATION_ID)
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }


    @Override
    public int getApplicationId() {
        return applicationId;
    }

    @Override
    public String getColor() {
        // grey color
        return "#A9A9A9";
    }


    public static class Columns {
        public static final String ID = "id";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String APPLICATION_ID = "application_id";
    }
}
