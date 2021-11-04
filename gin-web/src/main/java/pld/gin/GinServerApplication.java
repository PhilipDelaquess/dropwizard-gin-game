package pld.gin;

import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import pld.gin.engine.GinService;

/**
 * Created by philip on 8/3/16.
 */
public class GinServerApplication extends Application<GinServerConfiguration> {

    public static GinService GIN_SERVICE;

    public static void main (String[] args) throws Exception {
        new GinServerApplication().run(args);
    }

    @Override
    public void run(GinServerConfiguration ginServerConfiguration, Environment environment) throws Exception {

        environment.jersey().packages("pld.gin.rest");

        DBIFactory dbiFactory = new DBIFactory();
        DBI jdbi = dbiFactory.build(environment, ginServerConfiguration.getDatabase(), "h2");

        GIN_SERVICE = new GinService(jdbi);
    }

    @Override
    public void initialize (Bootstrap<GinServerConfiguration> bootstrap) {
        //bootstrap.addBundle(new ViewBundle<GinServerConfiguration>());
        bootstrap.addBundle(new ConfiguredAssetsBundle("/assets", "/"));
    }
}
