package com.lifeline.service;

import com.lifeline.entity.EmergencyAlert;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class AlertService {

    @PersistenceContext(unitName = "LifelinePU")
    private EntityManager em;

    public EmergencyAlert createAlert(EmergencyAlert alert) {
        em.persist(alert);
        return alert;
    }

    public List<EmergencyAlert> findAll() {
        return em.createQuery("SELECT a FROM EmergencyAlert a ORDER BY a.createdAt DESC", EmergencyAlert.class)
                 .getResultList();
    }

    public EmergencyAlert findById(Long id) {
        return em.find(EmergencyAlert.class, id);
    }

    public EmergencyAlert updateAlert(EmergencyAlert alert) {
        return em.merge(alert);
    }

    /** Update only the status field of an alert */
    public EmergencyAlert updateStatus(Long id, String status) {
        EmergencyAlert alert = em.find(EmergencyAlert.class, id);
        if (alert == null) return null;
        alert.setStatus(status);
        return em.merge(alert);
    }

    /** Delete a single alert by id */
    public boolean deleteAlert(Long id) {
        EmergencyAlert alert = em.find(EmergencyAlert.class, id);
        if (alert == null) return false;
        em.remove(alert);
        return true;
    }

    /** Delete all alerts in the system */
    public int deleteAll() {
        return em.createQuery("DELETE FROM EmergencyAlert").executeUpdate();
    }

    /** Return aggregate counts for the emergency dashboard */
    public Map<String, Long> getStats(Integer month, Integer year) {
        Map<String, Long> stats = new HashMap<>();

        String baseQuery = "SELECT COUNT(a) FROM EmergencyAlert a";
        String whereClause = "";

        if (month != null && year != null) {
            whereClause = " WHERE EXTRACT(MONTH FROM a.createdAt) = :month AND EXTRACT(YEAR FROM a.createdAt) = :year";
        }

        stats.put("total",    getCount(baseQuery + whereClause, month, year));
        stats.put("pending",  getCount(baseQuery + whereClause + (whereClause.isEmpty() ? " WHERE " : " AND ") + "a.status = 'pending'", month, year));
        stats.put("active",   getCount(baseQuery + whereClause + (whereClause.isEmpty() ? " WHERE " : " AND ") + "a.status = 'active'", month, year));
        stats.put("resolved", getCount(baseQuery + whereClause + (whereClause.isEmpty() ? " WHERE " : " AND ") + "a.status = 'resolved'", month, year));
        return stats;
    }

    private Long getCount(String q, Integer month, Integer year) {
        var query = em.createQuery(q, Long.class);
        if (month != null && year != null) {
            query.setParameter("month", month);
            query.setParameter("year", year);
        }
        return query.getSingleResult();
    }
}
