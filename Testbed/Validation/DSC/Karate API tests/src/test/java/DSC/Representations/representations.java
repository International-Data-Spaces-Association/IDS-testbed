package DSC.Representations;

import com.intuit.karate.junit5.Karate;

public class representations {

    @Karate.Test

    Karate testRepresentations(){
        return Karate.run("representations").relativeTo(getClass());
    }
}
