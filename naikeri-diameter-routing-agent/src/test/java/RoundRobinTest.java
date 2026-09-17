import com.naikeri.sgw.impl.app.model.Host;
import com.naikeri.sgw.impl.app.model.Rule;
import com.naikeri.sgw.impl.app.util.GettingRules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//import org.testng.AssertJUnit;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;

public class RoundRobinTest {

  //  @BeforeClass
    public void init() {
        GettingRules.initialize();
    }

    //@Test
    public void LoadBalancing() {
        Optional<Rule> ruleOptional = GettingRules.rules().list().stream()
                .filter(f -> f.match().originHost().matcher(".*").matches()
                        && f.match().originRealm().matcher("restcomm.org").matches()
                        && f.match().imsi().matcher("748026871012392").matches())
                .findFirst();
        if (ruleOptional.isPresent()) {
            Rule rule = ruleOptional.get();
            Map<String, Long> result = new HashMap<>();
            Map<String, String> resultArr = new HashMap<>();

            for (int i = 0; i < 300; i++) {
                Host host = rule.getHostByLoadBalance();
                host.incrementSentMessages();
                if (result.containsKey(host.getName())) {
                    result.put(host.getName(), result.get(host.getName())+1L);
                    resultArr.put(host.getName(), resultArr.get(host.getName())+", "+host.getSentMessages());
                } else {
                    result.put(host.getName(), host.getSentMessages());
                    resultArr.put(host.getName(), String.valueOf(host.getSentMessages()));
                }
            }

            for (Map.Entry<String, Long> kv : result.entrySet()) {
                System.out.printf("host: %s, count: %s, arr: [%s]%n",  kv.getKey(), kv.getValue(), resultArr.get(kv.getKey()));
            }
        } else {
           // AssertJUnit.assertTrue("Not Rule", false);

        }

    }
}