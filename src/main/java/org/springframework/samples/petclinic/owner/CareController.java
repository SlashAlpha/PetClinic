/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.care.Care;
import org.springframework.samples.petclinic.care.CareRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class CareController {

    private final CareRepository cares;
    private final PetRepository pets;


    public CareController(CareRepository cares, PetRepository pets) {
        this.cares = cares;
        this.pets = pets;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    /**
     * Called before each and every @RequestMapping annotated method.
     * 2 goals:
     * - Make sure we always have fresh data
     * - Since we do not use the session scope, make sure that Pet object always has an id
     * (Even though id is not part of the form fields)
     *
     * @param petId
     * @return Pet
     */
    @ModelAttribute("cares")
    public Care loadPetWithCare(@PathVariable("petId") int petId, Map<String, Object> model) {
        Pet pet = this.pets.findById(petId);
        pet.setCaresInternal(this.cares.findByPetId(petId));
        model.put("pet", pet);
        Care care = new Care();
        pet.addCare(care);

        return care;
    }

    // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
    @GetMapping("/owners/*/pets/{petId}/cares/{cares}/show")
    public String showCares(@PathVariable("petId") int petId, @PathVariable("cares") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Map<String, Object> model) {

        Pet pet = this.pets.findById(petId);
        pet.setCaresInternal(this.cares.findByPetId(petId));

        return "pets/showVisitCares";
    }

    // Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
    @PostMapping("/owners/{ownerId}/pets/{petId}/cares/new")
    public String processNewCareForm(@Valid Care care, BindingResult result) {
        if (result.hasErrors()) {
            return "pets/createOrUpdateCareForm";
        } else {
            this.cares.save(care);
            return "redirect:/owners/{ownerId}";
        }
    }

}
