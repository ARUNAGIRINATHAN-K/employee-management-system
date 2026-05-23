package com.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "performance_reviews")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonIgnoreProperties({"department", "manager", "shift"})
    private Employee reviewer;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @Column(name = "kpi_goals")
    private String kpiGoals;

    @Column(nullable = false)
    private Integer rating; // 1 to 5 rating scale

    private String comments;

    @Column(name = "promotion_recommendation")
    private Boolean promotionRecommendation;

    public PerformanceReview() {}

    public PerformanceReview(Long id, Employee employee, Employee reviewer, LocalDate reviewDate, String kpiGoals, Integer rating, String comments, Boolean promotionRecommendation) {
        this.id = id;
        this.employee = employee;
        this.reviewer = reviewer;
        this.reviewDate = reviewDate;
        this.kpiGoals = kpiGoals;
        this.rating = rating;
        this.comments = comments;
        this.promotionRecommendation = promotionRecommendation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getReviewer() {
        return reviewer;
    }

    public void setReviewer(Employee reviewer) {
        this.reviewer = reviewer;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getKpiGoals() {
        return kpiGoals;
    }

    public void setKpiGoals(String kpiGoals) {
        this.kpiGoals = kpiGoals;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Boolean getPromotionRecommendation() {
        return promotionRecommendation;
    }

    public void setPromotionRecommendation(Boolean promotionRecommendation) {
        this.promotionRecommendation = promotionRecommendation;
    }

    public static PerformanceReviewBuilder builder() {
        return new PerformanceReviewBuilder();
    }

    public static class PerformanceReviewBuilder {
        private Long id;
        private Employee employee;
        private Employee reviewer;
        private LocalDate reviewDate;
        private String kpiGoals;
        private Integer rating;
        private String comments;
        private Boolean promotionRecommendation;

        public PerformanceReviewBuilder id(Long id) { this.id = id; return this; }
        public PerformanceReviewBuilder employee(Employee employee) { this.employee = employee; return this; }
        public PerformanceReviewBuilder reviewer(Employee reviewer) { this.reviewer = reviewer; return this; }
        public PerformanceReviewBuilder reviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; return this; }
        public PerformanceReviewBuilder kpiGoals(String kpiGoals) { this.kpiGoals = kpiGoals; return this; }
        public PerformanceReviewBuilder rating(Integer rating) { this.rating = rating; return this; }
        public PerformanceReviewBuilder comments(String comments) { this.comments = comments; return this; }
        public PerformanceReviewBuilder promotionRecommendation(Boolean promotionRecommendation) { this.promotionRecommendation = promotionRecommendation; return this; }

        public PerformanceReview build() {
            return new PerformanceReview(id, employee, reviewer, reviewDate, kpiGoals, rating, comments, promotionRecommendation);
        }
    }
}
