package DSC.Catalogs;

import com.intuit.karate.junit5.Karate;

public class catalogs {

    @Karate.Test
    Karate testCatalogs() {
        return Karate.run("catalogs").relativeTo(getClass());
    } 
}
