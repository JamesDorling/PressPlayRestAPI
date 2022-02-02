package com.sparta.jd.springrest.controller;

import com.sparta.jd.springrest.entity.ActorEntity;
import com.sparta.jd.springrest.repository.ActorRepository;
import com.sparta.jd.springrest.services.TimestampMaker;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.ValidationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ActorController {
    private final ActorRepository ACTOR_REPOSITORY;
    private final Link link = Link.of("http://localhost:8080/actors");
    private final String BEFORE = "before", AFTER = "after", SAME_TIME = "same";

    @Autowired
    public ActorController(ActorRepository ACTOR_REPOSITORY) {
        this.ACTOR_REPOSITORY = ACTOR_REPOSITORY;
    }

    //@GetMapping("/actors")
    //public List<ActorEntity> getAllActors() {
    //    return ACTOR_REPOSITORY.findAll();
    //}

    @GetMapping("/actors") //This also throws a validation exception as it will be caught by the exception handler
    public List<EntityModel<ActorEntity>> getActorsList(@RequestParam(required = false, name = "name") String name,
                                                        @RequestParam(required = false, name = "timeOption") String timeOption,
                                                        @RequestParam(required = false, name = "time") String time,
                                                        @RequestParam(required = false, name = "firstName") String firstName,
                                                        @RequestParam(required = false, name = "lastName") String lastName ) throws ValidationException {
        return getActorsListByName(name, getActorsBasedOnTime(timeOption, Timestamp.valueOf(time),ACTOR_REPOSITORY.findAll()));
    }

    public List<EntityModel<ActorEntity>> getActorsListByName(String name, List<ActorEntity> entities) throws ValidationException {
        if(name == null) {
            return getAllActors();
        } else {
            List<EntityModel<ActorEntity>> foundActors = new ArrayList<>();
            for(ActorEntity actor : entities) {
                if ((actor.getFirstName().toLowerCase() + " " + actor.getLastName().toLowerCase()).contains(name.toLowerCase())) {
                    foundActors.add(EntityModel.of(actor,
                            linkTo(methodOn(ActorController.class).getActor(actor.getActorId())).withSelfRel(),
                            link.withRel("All Actors")
                    ));
                }
            }
            return foundActors;
        }
    }

    private List<EntityModel<ActorEntity>> getAllActors() throws ValidationException {
        List<EntityModel<ActorEntity>> foundActors = new ArrayList<>();
        for(ActorEntity actor : ACTOR_REPOSITORY.findAll()) {
            { foundActors.add(EntityModel.of(actor,
                    linkTo(methodOn(ActorController.class).getActor(actor.getActorId())).withSelfRel(),
                    link.withRel("All Actors")
            )); }
        }
        return foundActors;
    }

    @GetMapping("/actors/{id}")
    public EntityModel<ActorEntity> getActor(@PathVariable Integer id) throws ValidationException {
        ActorEntity actor = ACTOR_REPOSITORY.findById(id)
                .orElseThrow(() -> new ValidationException("No actor by that ID present in the database."));
        return EntityModel.of(
                actor,
                linkTo(methodOn(ActorController.class).getActor(id)).withSelfRel(),
                link.withRel("allActors"));
    }

    @PostMapping("/actors")
    public EntityModel<ActorEntity> addActor(@RequestBody ActorEntity actor) throws ValidationException {
        if(actor.getLastUpdate() == null) {
            actor.setLastUpdate(TimestampMaker.getCurrentDate());
        }
        if(actor.getActorId() == null && actor.getFirstName() != null && actor.getLastName() != null)
            return EntityModel.of(ACTOR_REPOSITORY.save(actor),
                    linkTo(methodOn(ActorController.class).getActor(actor.getActorId())).withSelfRel(),
                    link.withRel("allActors"));
        else throw new ValidationException("Actor cannot be created with provided data.");
    }

    @PutMapping("/actors/{id}")
    public ResponseEntity<ActorEntity> updateActor(@RequestBody ActorEntity actor, @PathVariable Integer id) throws ValidationException {
        return new ResponseEntity<ActorEntity>(ACTOR_REPOSITORY.findById(id).map(actorEntity -> {
            actorEntity.setFirstName(actor.getFirstName());
            actorEntity.setLastName(actor.getLastName());
            actorEntity.setLastUpdate(TimestampMaker.getCurrentDate());
            return ACTOR_REPOSITORY.save(actorEntity);
        }).orElseThrow(() -> new ValidationException("No actor by that ID present in the database.")), HttpStatus.OK);
    }

//    @GetMapping("/actors/before/{d}/{m}/{y}")
//    public List<EntityModel<ActorEntity>> getActorsUpdatedBefore(@PathVariable String d, @PathVariable String m, @PathVariable String y) throws ValidationException {
//        return getActorsBasedOnTime(BEFORE, Timestamp.valueOf(y + "-" + m + "-" + d + " 00:00:00"));
//    }
//
//    @GetMapping("/actors/after/{d}/{m}/{y}")
//    public List<EntityModel<ActorEntity>> getActorsUpdatedAfter(@PathVariable String d, @PathVariable String m, @PathVariable String y) throws ValidationException {
//        return getActorsBasedOnTime(AFTER, Timestamp.valueOf(y + "-" + m + "-" + d + " 00:00:00"));
//    }

    private List<EntityModel<ActorEntity>> getActorsBasedOnTime(String option, Timestamp time, List<ActorEntity> entities) throws ValidationException {
        List<EntityModel<ActorEntity>> foundEntities = new ArrayList<>();
        for (ActorEntity entity : entities) {
            switch(option) {
                case "after" -> {
                    if (entity.getLastUpdate().after(time)) {
                        foundEntities.add(EntityModel.of(entity,
                                linkTo(methodOn(ActorController.class).getActor(entity.getActorId())).withSelfRel(),
                                link.withRel("allActors")
                                ));
                    }
                }
                case "before" -> {
                    if(entity.getLastUpdate().before(time)) {
                        foundEntities.add(EntityModel.of(entity,
                                linkTo(methodOn(ActorController.class).getActor(entity.getActorId())).withSelfRel(),
                                link.withRel("allActors")
                                ));
                    }
                }
                case "same" -> {
                    if(entity.getLastUpdate().equals(time)) {
                        foundEntities.add(EntityModel.of(entity,
                                linkTo(methodOn(ActorController.class).getActor(entity.getActorId())).withSelfRel(),
                                link.withRel("allActors")
                        ));
                    }
                }
            }
        }
        return foundEntities.stream().toList();
    }

    @DeleteMapping("/actors/{id}")
    public void deleteActor(@PathVariable("id") Integer id) {
        ACTOR_REPOSITORY.deleteById(id);
    }
}
