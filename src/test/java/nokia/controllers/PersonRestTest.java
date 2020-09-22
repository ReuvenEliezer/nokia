package nokia.controllers;

import nokia.app.NokiaApp;
import nokia.entities.Person;
import nokia.utils.WsAddressConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = NokiaApp.class)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PersonRestTest {


    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void addPerson_Test() {
        Boolean isAdded = restTemplate.getForObject(WsAddressConstants.personFullUrl + "addPerson/1/person1", Boolean.class);
        Assert.assertTrue("unable to add person", isAdded);
    }


    @Test
    public void searchPersonTest() {
        restTemplate.getForObject(WsAddressConstants.personFullUrl + "addPerson/1/person", Boolean.class);
        List<Person> personA = restTemplate.getForObject(WsAddressConstants.personFullUrl + "searchPerson/person", List.class);
        Assert.assertEquals(1, personA.size());
    }

    @Test
    public void deletePersonTest() {
        restTemplate.getForObject(WsAddressConstants.personFullUrl + "addPerson/1/person", Boolean.class);

        int personDeletedSize = restTemplate.getForObject(WsAddressConstants.personFullUrl + "deletePerson/person", Integer.class);

        Assert.assertEquals(1, personDeletedSize);
    }


}
