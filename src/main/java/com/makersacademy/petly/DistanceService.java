package com.makersacademy.petly;

import org.springframework.stereotype.Service;

@Service
public class DistanceService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculateDistance(
            Double latitude1,
            Double longitude1,
            Double latitude2,
            Double longitude2) {

        double lat1 = Math.toRadians(latitude1);
        double lon1 = Math.toRadians(longitude1);
        double lat2 = Math.toRadians(latitude2);
        double lon2 = Math.toRadians(longitude2);

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1)
                * Math.cos(lat2)
                * Math.sin(deltaLon / 2)
                * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}

/*

This uses the Haversine formula to find distance between two points on a sphere.

Apparently it's a standard way of calculating the distance between coordinates.

 https://www.geeksforgeeks.org/dsa/haversine-formula-to-find-distance-between-two-points-on-a-sphere/

 */