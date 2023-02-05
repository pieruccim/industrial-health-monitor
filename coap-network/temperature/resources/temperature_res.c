#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "project-conf.h"
#include "coap-engine.h"

static float temp_value = 70.0;

//static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
//static void temp_event_handler();

static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temp_event_handler(void);

EVENT_RESOURCE(
    temperature_sensor,
    "title=\"Temperature sensor\";obs",
    get_temp_handler,
    NULL,
    NULL,
    NULL,
    temp_event_handler
);

void simulate_temp_sensor(){

    float coeff = 1.5;

    srand(time(NULL));
    float variation = ((float)rand() / (float)(RAND_MAX) * coeff);

    if(rand() % 2 == 0){
        temp_value += variation;
    } else{
        temp_value -= variation;
    }

    printf("Sensed temperature: %.2f C\n", temp_value);
    /* resource event is triggered, then temp_event_handler is called */
    temperature_sensor.trigger();
}

static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    /* converting temperature from float to string*/
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"temp\":%.2f}", temp_value);

    printf("payload: %s, length of payload: %d\n", buffer, strlen((char *)buffer));

    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_payload(response, buffer, strlen((char *)buffer));
}

static void temp_event_handler(void){

    // notification is sent to observers
    coap_notify_observers( &temperature_sensor); 
}