package DSC.Contracts;

import com.intuit.karate.junit5.Karate;

public class contracts {

    @Karate.Test

    Karate testContracts(){
        return Karate.run("contracts").relativeTo(getClass());
    }
}
