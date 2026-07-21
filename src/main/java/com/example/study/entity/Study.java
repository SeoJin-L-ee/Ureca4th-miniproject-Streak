package com.example.study.entity;

import java.util.Objects;

import com.example.global.entity.BaseEntity;
import com.example.study.dto.request.UpdateStudyReqDto;
import com.example.study.entity.enums.StudyCategory;
import com.example.study.entity.enums.StudyStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "studies")
public class Study extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudyCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudyStatus status;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    public void updateStudy(UpdateStudyReqDto reqDto) {
    	this.title = Objects.requireNonNullElse(reqDto.title(), this.title);
        this.description = Objects.requireNonNullElse(reqDto.description(), this.description);
        this.capacity = Objects.requireNonNullElse(reqDto.capacity(), this.capacity);
        this.category = Objects.requireNonNullElse(reqDto.category(), this.category);
    }
    
    public void updateStatus(StudyStatus status) {
    	this.status = status;
    }
    
    public void updateIsDeleted(boolean isDeleted) {
    	this.isDeleted = isDeleted;
    }
}
