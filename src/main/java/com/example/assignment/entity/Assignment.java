package com.example.assignment.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.example.assignment.dto.request.UpdateAssignmentReqDto;
import com.example.global.entity.BaseEntity;
import com.example.session.entity.Session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignments")
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;
    
    public void updateAssignment(UpdateAssignmentReqDto reqDto) {
    	this.title = Objects.requireNonNullElse(reqDto.title(), this.title);
    	this.description = Objects.requireNonNullElse(reqDto.description(), this.description);
    	this.dueAt = Objects.requireNonNullElse(reqDto.dueAt(), this.dueAt);
    }
}