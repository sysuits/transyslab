import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import javax.inject.Singleton;

public class Test {
    @Singleton
    static Printer printer;

    public static void main(String[] args) {
        Injector in = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                binder().requestStaticInjection(Test.class);
            }
        });
        Test t = in.getInstance(Test.class);
        printer.sayHi();
    }
}

class MyPrinter implements Printer{

    @Override
    public void sayHi() {
        System.out.println("Hi my printer.");
    }
}
