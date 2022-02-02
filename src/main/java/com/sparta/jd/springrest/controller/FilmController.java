package com.sparta.jd.springrest.controller;

import com.sparta.jd.springrest.entity.ActorEntity;
import com.sparta.jd.springrest.entity.FilmEntity;
import com.sparta.jd.springrest.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class FilmController {
    private final FilmRepository FILM_REPOSITORY;
    private final Link link = Link.of("http://localhost:8080/films");

    @Autowired
    public FilmController(FilmRepository filmRepository) {
        this.FILM_REPOSITORY = filmRepository;
    }

    @GetMapping("/films")
    public List<EntityModel<FilmEntity>> getFilmList(@RequestParam(required = false, name = "name") String name) throws ValidationException {
        if(name == null) {
            return getAllFilms();
        } else {
            List<EntityModel<FilmEntity>> foundFilms = new ArrayList<>();
            for(FilmEntity film : FILM_REPOSITORY.findAll()) {
                if (film.getTitle().contains(name.toLowerCase())) {
                    foundFilms.add(EntityModel.of(film,
                            linkTo(methodOn(FilmController.class).getFilm(film.getFilmId())).withSelfRel(),
                            link.withRel("All Films")
                    ));
                }
            }
            return foundFilms;
        }
    }

    private List<EntityModel<FilmEntity>> getAllFilms() throws ValidationException {
        List<EntityModel<FilmEntity>> foundFilms = new ArrayList<>();
        for(FilmEntity film : FILM_REPOSITORY.findAll()) {
            {
                foundFilms.add(EntityModel.of(film,
                    linkTo(methodOn(FilmController.class).getFilm(film.getFilmId())).withSelfRel(),
                    link.withRel("All Films")
                ));
            }
        }
        return foundFilms;
    }

    @GetMapping("/film/{id}")
    private EntityModel<FilmEntity> getFilm(@PathVariable("id") Integer id) throws ValidationException {
        FilmEntity film = FILM_REPOSITORY.findById(id)
                .orElseThrow(() -> new ValidationException("No film by that ID present in the database."));
        return EntityModel.of(
                film,
                linkTo(methodOn(FilmController.class).getFilm(id)).withSelfRel(),
                link.withRel("All Films"));
    }
}
