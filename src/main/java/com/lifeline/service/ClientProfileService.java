package com.lifeline.service;

import com.lifeline.entity.ClientProfile;
import com.lifeline.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Service for CLIENT_PROFILES table CRUD.
 */
@Stateless
public class ClientProfileService {

    @PersistenceContext(unitName = "LifelinePU")
    private EntityManager em;

    /** Find profile by the linked User entity. */
    public ClientProfile findByUser(User user) {
        if (user == null) return null;
        try {
            return em.createQuery(
                    "SELECT cp FROM ClientProfile cp WHERE cp.user = :user",
                    ClientProfile.class)
                    .setParameter("user", user)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /** Find profile by user numeric id. */
    public ClientProfile findByUserId(Long userId) {
        try {
            return em.createQuery(
                    "SELECT cp FROM ClientProfile cp WHERE cp.user.numericId = :uid",
                    ClientProfile.class)
                    .setParameter("uid", userId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /** Create or update a client profile. */
    public ClientProfile save(ClientProfile profile) {
        if (profile.getId() == null) {
            em.persist(profile);
            return profile;
        }
        return em.merge(profile);
    }

    /**
     * Upsert: if a profile exists for this user, update its fields;
     * otherwise create a new one.
     */
    public ClientProfile upsert(User user, ClientProfile incoming) {
        ClientProfile existing = findByUser(user);
        if (existing == null) {
            incoming.setUser(user);
            em.persist(incoming);
            return incoming;
        }
        if (incoming.getPhone() != null)         existing.setPhone(incoming.getPhone());
        if (incoming.getInsurancePolicy() != null) existing.setInsurancePolicy(incoming.getInsurancePolicy());
        if (incoming.getVehicleMake() != null)   existing.setVehicleMake(incoming.getVehicleMake());
        if (incoming.getVehicleModel() != null)  existing.setVehicleModel(incoming.getVehicleModel());
        if (incoming.getVehicleYear() != null)   existing.setVehicleYear(incoming.getVehicleYear());
        if (incoming.getLicensePlate() != null)  existing.setLicensePlate(incoming.getLicensePlate());
        return em.merge(existing);
    }

    public List<ClientProfile> findAll() {
        return em.createQuery("SELECT cp FROM ClientProfile cp", ClientProfile.class).getResultList();
    }
}
