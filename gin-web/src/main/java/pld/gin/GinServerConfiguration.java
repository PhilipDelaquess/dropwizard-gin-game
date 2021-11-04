package pld.gin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.bundles.assets.AssetsBundleConfiguration;
import io.dropwizard.bundles.assets.AssetsConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by philip on 8/3/16.
 */
public class GinServerConfiguration extends Configuration implements AssetsBundleConfiguration {
    @JsonProperty
    private String copyright;

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty
    private final AssetsConfiguration assets = AssetsConfiguration.builder().build();

    public String getCopyright () {
        return copyright;
    }

    public void setCopyright (String c) {
        copyright = c;
    }

    public DataSourceFactory getDatabase () {
        return database;
    }

    public void setDatabase (DataSourceFactory dsf) {
        database = dsf;
    }

    @Override
    public AssetsConfiguration getAssetsConfiguration () {
        return assets;
    }
}
