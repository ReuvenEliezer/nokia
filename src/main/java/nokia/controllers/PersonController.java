package nokia.controllers;

import nokia.entities.Person;
import nokia.services.PersonManager;
import nokia.utils.WsAddressConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(WsAddressConstants.personLogicUrl)
public class PersonController {

    @Autowired
    private PersonManager personManager;

    @GetMapping(value = "addPerson/{id}/{name}")
    public Boolean addPerson(@PathVariable String id, @PathVariable String name) {
        return personManager.addPerson(id, name);
    }

    @GetMapping(value = "deletePerson/{name}")
    public int deletePerson(@PathVariable String name) {
        return personManager.deletePerson(name);
    }

    @GetMapping(value = "searchPerson/{name}")
    public List<Person> searchPerson(@PathVariable String name) {
        return personManager.searchPerson(name);
    }
}
