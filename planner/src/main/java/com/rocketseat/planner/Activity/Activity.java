package com.rocketseat.planner.Activity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


import com.rocketseat.planner.Trip.Trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Activity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "occurs_at", nullable= false)
  private LocalDateTime occursAt;

  @Column(nullable= false)
  private String title;

  @ManyToOne
  @JoinColumn(name = "trip_id", nullable = false)
  private Trip trip;

  public Activity(String title, String occusAt, Trip trip){
    this.title = title;
    this.occursAt = LocalDateTime.parse(occusAt, DateTimeFormatter.ISO_DATE_TIME);
    this.trip = trip;
  }
}
