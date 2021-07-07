package DSC.Rules;

import com.intuit.karate.junit5.Karate;

public class rules {

    @Karate.Test
    Karate testRules() {
        return Karate.run("rules").relativeTo(getClass());
    } 
}
