package com.legacy.report.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReportTest {

    @Test
    void shouldCreateReportWithAllFields() {
        // Given
        Long id = 1L;
        String name = "Test Report";
        String sql = "SELECT * FROM test";
        String description = "Test Description";
        
        // When
        Report report = new Report(id, name, sql, description);
        
        // Then
        assertEquals(id, report.getId());
        assertEquals(name, report.getName());
        assertEquals(sql, report.getSql());
        assertEquals(description, report.getDescription());
    }

    @Test
    void shouldSetAndGetId() {
        // Given
        Report report = new Report();
        Long id = 123L;
        
        // When
        report.setId(id);
        
        // Then
        assertEquals(id, report.getId());
    }

    @Test
    void shouldSetAndGetName() {
        // Given
        Report report = new Report();
        String name = "Customer Report";
        
        // When
        report.setName(name);
        
        // Then
        assertEquals(name, report.getName());
    }

    @Test
    void shouldSetAndGetSql() {
        // Given
        Report report = new Report();
        String sql = "SELECT * FROM customer WHERE status = 'active'";
        
        // When
        report.setSql(sql);
        
        // Then
        assertEquals(sql, report.getSql());
    }

    @Test
    void shouldSetAndGetDescription() {
        // Given
        Report report = new Report();
        String description = "Customer activity report for active users";
        
        // When
        report.setDescription(description);
        
        // Then
        assertEquals(description, report.getDescription());
    }

    @Test
    void shouldHandleNullName() {
        // Given
        Report report = new Report();
        
        // When
        report.setName(null);
        
        // Then
        assertNull(report.getName());
    }

    @Test
    void shouldHandleEmptyName() {
        // Given
        Report report = new Report();
        
        // When
        report.setName("");
        
        // Then
        assertEquals("", report.getName());
    }

    @Test
    void shouldHandleNullSql() {
        // Given
        Report report = new Report();
        
        // When
        report.setSql(null);
        
        // Then
        assertNull(report.getSql());
    }

    @Test
    void shouldHandleEmptySql() {
        // Given
        Report report = new Report();
        
        // When
        report.setSql("");
        
        // Then
        assertEquals("", report.getSql());
    }

    @Test
    void shouldHandleNullDescription() {
        // Given
        Report report = new Report();
        
        // When
        report.setDescription(null);
        
        // Then
        assertNull(report.getDescription());
    }

    @Test
    void shouldHandleEmptyDescription() {
        // Given
        Report report = new Report();
        
        // When
        report.setDescription("");
        
        // Then
        assertEquals("", report.getDescription());
    }

    @Test
    void shouldHandleComplexSql() {
        // Given
        Report report = new Report();
        String complexSql = """
            SELECT c.name, c.email, COUNT(o.id) as order_count
            FROM customer c
            LEFT JOIN orders o ON c.id = o.customer_id
            WHERE c.status = 'active'
            AND o.created_at >= '2023-01-01'
            GROUP BY c.id, c.name, c.email
            ORDER BY order_count DESC
            """;
        
        // When
        report.setSql(complexSql);
        
        // Then
        assertEquals(complexSql, report.getSql());
    }

    @Test
    void shouldHandleSpecialCharactersInName() {
        // Given
        Report report = new Report();
        String nameWithSpecialChars = "Report #1: Customer's Data (2023)";
        
        // When
        report.setName(nameWithSpecialChars);
        
        // Then
        assertEquals(nameWithSpecialChars, report.getName());
    }

    @Test
    void shouldHandleLongDescription() {
        // Given
        Report report = new Report();
        String longDescription = """
            This is a comprehensive report that analyzes customer behavior patterns,
            including purchase history, frequency of orders, average order value,
            customer lifetime value, and various other metrics that help in
            understanding customer engagement and retention strategies.
            """;
        
        // When
        report.setDescription(longDescription);
        
        // Then
        assertEquals(longDescription, report.getDescription());
    }

    @Test
    void shouldAllowFieldUpdates() {
        // Given
        Report report = new Report(1L, "Original Name", "SELECT * FROM original", "Original Description");
        
        // When
        report.setId(2L);
        report.setName("Updated Name");
        report.setSql("SELECT * FROM updated");
        report.setDescription("Updated Description");
        
        // Then
        assertEquals(2L, report.getId());
        assertEquals("Updated Name", report.getName());
        assertEquals("SELECT * FROM updated", report.getSql());
        assertEquals("Updated Description", report.getDescription());
    }

    @Test
    void shouldHandleZeroId() {
        // Given
        Report report = new Report();
        
        // When
        report.setId(0L);
        
        // Then
        assertEquals(0L, report.getId());
    }

    @Test
    void shouldHandleNegativeId() {
        // Given
        Report report = new Report();
        
        // When
        report.setId(-1L);
        
        // Then
        assertEquals(-1L, report.getId());
    }
}
