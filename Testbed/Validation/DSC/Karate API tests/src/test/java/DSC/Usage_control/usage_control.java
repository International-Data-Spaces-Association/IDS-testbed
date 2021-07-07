package DSC.Usage_control;

import com.intuit.karate.junit5.Karate;

public class usage_control {

    @Karate.Test
    Karate testUsage_Control() {
        return Karate.run("usage_control").relativeTo(getClass());
    } 
}
