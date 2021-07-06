package DSC.Resources;

import com.intuit.karate.junit5.Karate;

public class resources {

    @Karate.Test
    Karate testResources() {
        return Karate.run("resources").relativeTo(getClass());
    } 
}
