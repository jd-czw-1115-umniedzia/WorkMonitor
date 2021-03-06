package pl.edu.agh.io.umniedziala.viewController;

import pl.edu.agh.io.umniedziala.model.ApplicationEntity;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagingApplicationsController {

    private static final Logger logger = Logger.getLogger(ManagingApplicationsController.class.getName());

    public boolean addNewApplicationByPath(String path, String color) {
        if (path.length() <= 0) {
            logger.log(Level.WARNING, "Empty path.");

            return false;
        } else {
            String appName = path.split("\\\\")[path.split("\\\\").length - 1];

            if (ApplicationEntity.findByName(appName).isPresent()) {
                logger.info("This application is already added: " + appName);
                return false;
            } else {
                ApplicationEntity.create(appName, path, color);
                logger.info(String.format("Added %s with path: %s", appName, path));
                return true;
            }
        }
    }

    public void deleteApplication(String path) throws IllegalArgumentException, IllegalStateException, SQLException {
        if (path.length() <= 0) {
            logger.log(Level.WARNING, "Empty path.");
            throw new IllegalArgumentException("Empty path");
        } else {
            Optional<ApplicationEntity> application = ApplicationEntity.findByApplicationPath(path);

            if (!application.isPresent()) {
                logger.log(Level.WARNING, String.format("Application: %s not observed", path));
                throw new IllegalStateException("Application not in database");
            } else {
                ApplicationEntity.delete(application.get().getId());
            }
        }
    }
}
