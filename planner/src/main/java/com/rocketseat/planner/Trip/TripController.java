package com.rocketseat.planner.Trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocketseat.planner.Activity.AcitivityRequestPayload;
import com.rocketseat.planner.Activity.ActivityData;
import com.rocketseat.planner.Activity.ActivityResponse;
import com.rocketseat.planner.Activity.ActivityService;
import com.rocketseat.planner.Link.LinkData;
import com.rocketseat.planner.Link.LinkRequestPayload;
import com.rocketseat.planner.Link.LinkResponse;
import com.rocketseat.planner.Link.LinkService;
import com.rocketseat.planner.participant.ParticipantCreateResponse;
import com.rocketseat.planner.participant.ParticipantData;
import com.rocketseat.planner.participant.ParticipantRequestPayload;
import com.rocketseat.planner.participant.ParticipantService;

@RestController
@RequestMapping("/trips")
public class TripController {

  @Autowired
  private ParticipantService participantService;

  @Autowired
  private TripRepository repository;

  @Autowired
  private ActivityService activityService;

  @Autowired
  private LinkService linkService;

  //Trips

  @PostMapping
  public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
    Trip newTrip = new Trip(payload);

    this.repository.save(newTrip);
    this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

    return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
    Optional<Trip> trip = this.repository.findById(id);

    return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(id);

    if (trip.isPresent()) {
      Trip rowTrip = trip.get();
      rowTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
      rowTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
      rowTrip.setDestination(payload.destination());

      this.repository.save(rowTrip);

      return ResponseEntity.ok(rowTrip);
    }

    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}/confirm")
  public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
    Optional<Trip> trip = this.repository.findById(id);

    if (trip.isPresent()) {
      Trip rowTrip = trip.get();
      rowTrip.setIsConfirmed(true);

      this.repository.save(rowTrip);
      this.participantService.triggerConfirmationEmailToParticipants(id);

      return ResponseEntity.ok(rowTrip);
    }

    return ResponseEntity.notFound().build();
  }
  
  //Activity

  @PostMapping("/{id}/activities")
  public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id,
      @RequestBody AcitivityRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(id);

    if (trip.isPresent()) {
      Trip rowTrip = trip.get();

      ActivityResponse activityResponse = this.activityService.registerActivity(payload, rowTrip);

      return ResponseEntity.ok(activityResponse);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}/activities")
  public ResponseEntity<List<ActivityData>> getActivities(@PathVariable UUID id) {
    List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromId(id);

    return ResponseEntity.ok(activityDataList);
  };
  //participant

  @PostMapping("/{id}/invite")
  public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id,
      @RequestBody ParticipantRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(id);

    if (trip.isPresent()) {
      Trip rowTrip = trip.get();

      ParticipantCreateResponse participantResponse = this.participantService
          .registerParticipantToEvent(payload.email(), rowTrip);

      if (rowTrip.getIsConfirmed())
        this.participantService.triggerConfirmationEmailToParticipants(payload.email());

      return ResponseEntity.ok(participantResponse);
    }

    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}/participants")
  public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
    List<ParticipantData> participants = this.participantService.getAllParticipantsFromEvent(id);

    return ResponseEntity.ok(participants);
  };

  //Links
  @PostMapping("/{id}/links")
  public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id,
      @RequestBody LinkRequestPayload payload) {
    Optional<Trip> trip = this.repository.findById(id);

    if (trip.isPresent()) {
      Trip rowTrip = trip.get();

      LinkResponse LinkResponse = this.linkService.registerLink(payload, rowTrip);

      return ResponseEntity.ok(LinkResponse);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}/links")
  public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id) {
    List<LinkData> LinkDataList = this.linkService.getAllLinksFromId(id);

    return ResponseEntity.ok(LinkDataList);
  };
}
