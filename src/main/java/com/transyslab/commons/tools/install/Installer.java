package com.transyslab.commons.tools.install;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class Installer {
    private static Injector injector;
    private static Module module;

    public static Injector getInjector() {
        if (injector==null)
            injector = Guice.createInjector(getModule());
        return injector;
    }

    public static <T> T getInstance(Class<T> tClass){
        return getInjector().getInstance(tClass);
    }

    protected static Module getModule(){
        if (module==null)
            module = new RootModule();
        return module;
    }

    public static void overrideModule(AbstractModule newModule){
        module = Modules.override(getModule()).with(newModule);
        if (injector!=null){
            System.out.println("Warning: Injector created. Force to override.");
            injector = Guice.createInjector(getModule());
        }
    }

    public static boolean injected(){
        return injector!=null;
    }
}
