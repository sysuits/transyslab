package com.transyslab.commons.tools.install;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.transyslab.commons.io.JdbcInoutRec;
import com.transyslab.commons.io.Spatiotemporal;
import com.transyslab.commons.io.db.GeneralDB;
import com.transyslab.commons.io.db.JdbcUtils;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;

public class RootModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GeneralDB.class).to(JdbcUtils.class);
        bind(Spatiotemporal.class).annotatedWith(Names.named("InoutRec")).to(JdbcInoutRec.class);
        bind(SimulationEngine.class).to(MLPEngine.class);
    }
}
