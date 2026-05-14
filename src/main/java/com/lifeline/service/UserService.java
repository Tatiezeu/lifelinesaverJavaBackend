package com.lifeline.service;

import com.lifeline.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class UserService {

    @PersistenceContext(unitName = "LifelinePU")
    private EntityManager em;

    public User createUser(User user) {
        em.persist(user);
        return user;
    }

    public User findByEmail(String email) {
        if (email == null) return null;
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                     .setParameter("email", email)
                     .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.dateJoined DESC", User.class).getResultList();
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public User updateUser(User updated) {
        User existing = findByEmail(updated.getEmail());
        if (existing == null) return null;
        if (updated.getName() != null)  existing.setName(updated.getName());
        if (updated.getRole() != null)  existing.setRole(updated.getRole());
        if (updated.getPassword() != null && !updated.getPassword().isBlank())
            existing.setPassword(updated.getPassword());
        if (updated.getProfilePicture() != null)
            existing.setProfilePicture(updated.getProfilePicture());
        return em.merge(existing);
    }

    public boolean deleteUser(String email) {
        User user = findByEmail(email);
        if (user == null) return false;
        em.remove(user);
        return true;
    }

    public User toggleSuspend(String email) {
        User user = findByEmail(email);
        if (user == null) return null;
        user.setSuspended(!user.isSuspended());
        return em.merge(user);
    }
}
