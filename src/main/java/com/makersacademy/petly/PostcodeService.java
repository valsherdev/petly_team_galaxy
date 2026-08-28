package com.makersacademy.petly;

import com.makersacademy.petly.model.Service;
import com.makersacademy.petly.model.User;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@org.springframework.stereotype.Service
public class PostcodeService {

    private final RestClient restClient;

    public PostcodeService(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.postcodes.io")
                .build();
    }

    public boolean setCoordinates(Service service) {

        String postcode = service.getLocation()
                .trim()
                .toUpperCase()
                .replace(" ", "");

        try {
            PostcodeResponse response = restClient.get()
                    .uri("/postcodes/{postcode}", postcode)
                    .retrieve()
                    .body(PostcodeResponse.class);

            if (response == null || response.getResult() == null) {
                return false;
            }

            service.setLocation(response.getResult().getPostcode());
            service.setLatitude(response.getResult().getLatitude());
            service.setLongitude(response.getResult().getLongitude());

            return true;

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public boolean setCoordinates(User user) {

        String postcode = user.getLocation()
                .trim()
                .toUpperCase()
                .replace(" ", "");

        try {
            PostcodeResponse response = restClient.get()
                    .uri("/postcodes/{postcode}", postcode)
                    .retrieve()
                    .body(PostcodeResponse.class);

            if (response == null || response.getResult() == null) {
                return false;
            }

            user.setLocation(response.getResult().getPostcode());
            user.setLatitude(response.getResult().getLatitude());
            user.setLongitude(response.getResult().getLongitude());

            return true;

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private static class PostcodeResponse {

        private PostcodeData result;

        public PostcodeData getResult() {
            return result;
        }

        public void setResult(PostcodeData result) {
            this.result = result;
        }
    }

    private static class PostcodeData {

        private String postcode;
        private Double latitude;
        private Double longitude;

        public String getPostcode() {
            return postcode;
        }

        public void setPostcode(String postcode) {
            this.postcode = postcode;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}

/*

 The API we are integrating here is:
https://api.postcodes.io/

 */