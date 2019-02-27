package com.viadee.sonarquest.services;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viadee.sonarquest.entities.Artefact;
import com.viadee.sonarquest.entities.Level;
import com.viadee.sonarquest.entities.User;
import com.viadee.sonarquest.repositories.ArtefactRepository;

@Service
public class ArtefactService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtefactService.class);

    @Autowired
    private ArtefactRepository artefactRepository;

    @Autowired
    private LevelService levelService;

    @Autowired
    private UserService userService;

    public List<Artefact> getArtefacts() {
        return artefactRepository.findAll();
    }

    public List<Artefact> getArtefactsforMarkteplace() {
        return artefactRepository.findByQuantityIsGreaterThanEqual((long) 1);
    }

    public Artefact getArtefact(final long id) {
        return artefactRepository.findOne(id);
    }

    @Transactional
    public Artefact createArtefact(final Artefact artefact) {
        LOGGER.info("Creating new artefact " + artefact.getName());
        Level minLevel = artefact.getMinLevel();
        Level existingLevel = levelService.findByLevel(minLevel.getLevelNumber());
        if (existingLevel != null) {
            artefact.setMinLevel(existingLevel);
        } else {
            LOGGER.info("Artefact Level " + minLevel.getLevelNumber() + " does not exist yet - creating it...");
            levelService.createLevel(minLevel);
            artefact.setMinLevel(minLevel);
        }
        return artefactRepository.save(artefact);
    }

    @Transactional
    public Artefact updateArtefact(final Long id, final Artefact artefactDto) {
        LOGGER.info("Updating artefact " + id);
        final Artefact artefact = artefactRepository.findOne(id);
        artefact.setName(artefactDto.getName());
        artefact.setIcon(artefactDto.getIcon());
        artefact.setPrice(artefactDto.getPrice());
        artefact.setDescription(artefactDto.getDescription());
        artefact.setQuantity(artefactDto.getQuantity());
        artefact.setSkills(artefactDto.getSkills());
        int minLevel = artefactDto.getMinLevel().getLevelNumber();
        Level newLevel = levelService.findByLevel(minLevel);
        artefact.setMinLevel(newLevel);
        return artefactRepository.save(artefact);
    }

    @Transactional
    public synchronized Artefact buyArtefact(Artefact artefact, final User user) {
        LOGGER.info("User " + user.getId() + " tries to buy artefact " + artefact.getId());
        // If developer has TOO LITTLE GOLD, Then the purchase is canceled
        final long gold = user.getGold() - artefact.getPrice();
        if (gold < 0) {
            return null;
        }

        // If the developer has ALREADY BOUGHT the Artefact, Then the purchase is
        // canceled
        for (final Artefact a : user.getArtefacts()) {
            if (a.equals(artefact)) {
                return null;
            }
        }

        // If the artefact is SOLD OUT, then the purchase is canceled
        if (artefact.getQuantity() < 1) {
            return null;
        }

        // When the LEVEL of the developer is too low, then the purchase is canceled
        final Level minLevel = artefact.getMinLevel();
        final Level devLevel = user.getLevel();

        if (minLevel.getLevelNumber() > devLevel.getLevelNumber()) {
            return null;
        }

        user.getArtefacts().add(artefact);
        user.setGold(gold);
        userService.save(user);

        artefact.setQuantity(artefact.getQuantity() - 1);
        artefact = artefactRepository.save(artefact);
        LOGGER.info("User " + user.getId() + " successfully bought artefact " + artefact.getId());
        return artefact;
    }

}
