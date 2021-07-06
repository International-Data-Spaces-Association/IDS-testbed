package DSC.Artifacts;

import com.intuit.karate.junit5.Karate;

public class artifacts {

    @Karate.Test
    Karate testArtifacts() {
        return Karate.run("artifacts").relativeTo(getClass());
    } 
}
