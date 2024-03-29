#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "project-conf.h"
#include "coap-engine.h"

#include "sys/log.h"
#define LOG_MODULE "temp-sensor"
#define LOG_LEVEL LOG_LEVEL_APP

#define MAX_TEMP 100
#define MIN_TEMP 20

static float temp_value = 70.0;
static bool cooler_activated = false;

//static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
//static void temp_event_handler();

static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temp_event_handler(void);

EVENT_RESOURCE(
    temperature_sensor,
    "title=\"Temperature sensor\";obs",
    get_temp_handler,
    NULL,
    put_temp_handler,
    NULL,
    temp_event_handler
);

void simulate_temp_sensor(){

    float coeff = 3.0;

    srand(time(NULL));
    float variation = ((float)rand() / (float)(RAND_MAX) * coeff);

    if (cooler_activated){

        /* temperature decreases */
        temp_value -= variation;

    } else {

        if(rand() % 2 == 0){
            temp_value += variation;
        } else{
            temp_value -= variation;
        }

    }

    LOG_INFO("Sensed temperature: %.2f C\n", temp_value);
    /* resource event is triggered, then temp_event_handler is called */
    temperature_sensor.trigger();
}

static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    /* converting temperature from float to string*/
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"temp\":%.2f}", temp_value);

    LOG_INFO("Sending payload: %s, length of payload: %d\n", buffer, strlen((char *)buffer));

    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_payload(response, buffer, strlen((char *)buffer));
}

static void put_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    int len = 0;
    const uint8_t *payload;

    LOG_INFO("Temperature sensor received put request\n");

    if ((len = coap_get_payload(request, &payload))){
        
        /* put request tells the sensor if the cooler is activated or not */
        char data[30];
        strncpy(data, (char*) payload, len);
        data[len] = '\0';

        LOG_INFO("Temperature sensor received payload: %s\n", data);

        if (strcmp( "activate", data) == 0){

            cooler_activated = true;
        } else if (strcmp( "deactivate", data) == 0){
            
            cooler_activated = false;
        }
    }
}

static void temp_event_handler(void){

    // notification is sent to observers
    coap_notify_observers( &temperature_sensor); 
}