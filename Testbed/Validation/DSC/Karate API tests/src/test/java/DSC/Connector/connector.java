package DSC.Connector;

import com.intuit.karate.junit5.Karate;

public class connector {

    @Karate.Test
    Karate testConnector() {
        return Karate.run("connector").relativeTo(getClass());
    } 
}
