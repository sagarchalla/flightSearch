package com.example.flightsearch;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOffer;
import com.google.gson.JsonObject;
import models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@SessionAttributes("flightSearchForm")
public class FlightController {

    // Create logger class
    private static final Logger log = LogManager.getLogger(FlightController.class);

    @Value("${amadeus.clientId}")
    private String clientId;

    @Value("${amadeus.secretKey}")
    private String secretKey;

    @ModelAttribute("flightSearchForm")
    public FlightSearchForm setUpFlightSearchForm() {
        // Pre populate airline codes TODO
        return new FlightSearchForm();
    }

    @GetMapping("/flightSearch")
    public String getGreeting(Model model, @ModelAttribute("flightSearchForm") FlightSearchForm flightSearchForm) {
        model.addAttribute("flightSearchForm", flightSearchForm);
        return "flightSearchPage";
    }

    @PostMapping("/flightSearch")
    public String getFlightInfo(@ModelAttribute("flightSearchForm") FlightSearchForm flightSearchForm, Model model) {

        FlightOffer[] flightOffers = null;

        // Format flight departure date
        String departureDate = parseFlightDate(flightSearchForm.getFromDate());

        try {
            // Flight API search
            flightOffers = getAmamdeus().shopping.flightOffers.get(Params
                    .with("origin", flightSearchForm.getFromDestinations())
                    .and("destination", flightSearchForm.getToDestinations())
                    .and("departureDate", departureDate)
                    .and("nonStop", true));
        } catch (ResponseException e) {
            if (log.isDebugEnabled())
                log.debug("Flight Offer Response Failed : " + e.getCause());
        }

        // Get carrier codes from the JSON object
        JsonObject jsonObject = Arrays.asList(flightOffers).get(0).getResponse().getResult().getAsJsonObject("dictionaries").getAsJsonObject("carriers");

        List<FlightDetailsForm> flightDetailsList = new ArrayList<>();

        List<FlightOffer.Segment> segments = Arrays.stream(flightOffers).flatMap(flightOffer -> Arrays.stream(flightOffer.getOfferItems()))
                .flatMap(offerItem -> Stream.of(offerItem.getServices())
                        .flatMap(service -> Stream.of(service.getSegments())))
                .collect(Collectors.toList());


        for (FlightOffer.Segment segment : segments) {

            FlightDetailsForm flightDetailsForm = new FlightDetailsForm();
            FlightOffer.FlightSegment flightSegment = segment.getFlightSegment();



            flightDetailsForm.setAirline(jsonObject.get(flightSegment.getCarrierCode()).getAsString());
            flightDetailsForm.setDepartureTime(flightSegment.getDeparture().getAt().substring(11, 16));
            flightDetailsForm.setDuration(parseDuration(flightSegment.getDuration()));
            flightDetailsForm.setArrival(flightSegment.getArrival().getIataCode());
            flightDetailsForm.setArrivalTime(flightSegment.getArrival().getAt().substring(11, 16));
            flightDetailsForm.setDeparture(flightSegment.getDeparture().getIataCode());
            flightDetailsForm.setAvailability(segment.getPricingDetailPerAdult().getAvailability());
            flightDetailsList.add(flightDetailsForm);
        }
        Collections.reverse(flightDetailsList);

        model.addAttribute("flightDetailsList", flightDetailsList);
        return "flightDetailsPage";
    }

    private Amadeus getAmamdeus() {
        // Call amadeus with the submitted form data
        return Amadeus
                .builder(clientId, secretKey)
                .build();
    }

    private String parseFlightDate(Date departureDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(departureDate);
    }

    private String parseDuration(String duration) {
        return duration.replace("0DT", "").replace("H", "H ").replace("M", "M ");
    }

}
