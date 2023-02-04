#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "project-conf.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static bool activated = false;

static void put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(
    cooler_actuator,
    "title=\"Cooler actuator\";rt=\"Control\"",
    NULL,
    NULL,
    put_handler,
    NULL
);

static void put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    int len = 0;
    const uint8_t *payload;

    if (len = coap_get_payload(request, &payload)){
        
        /* activate or deactivate cooler depending on request */
        char data[30];
        strncpy(data, (char*) payload, len);

        LOG_INFO("Cooler received put request: %s", data);

        if (strcmp( "activate", data) == 0){
            
            LOG_INFO("Activating cooler...");
            activated = true;
            leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));

        } else if (strcmp( "deactivate", data) == 0){
            
            LOG_INFO("Deactivating cooler...");
            activated = false;
            leds_set(LEDS_NUM_TO_MASK(LEDS_RED));

        }
    }
}