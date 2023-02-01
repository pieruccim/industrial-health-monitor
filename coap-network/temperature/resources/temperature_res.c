
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "coap-engine.h"

static float temp_value = 70.0;
float temp_threshold = 90.0;

//static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
//static void temp_event_handler();

static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temp_event_handler(void);

static float simulate_temp_sensor(){

    float coeff = 1.5;

    srand(time(NULL));
    float variation = ((float)rand() / (float)(RAND_MAX) * coeff);

    if(rand() % 2 == 0){
        return temp_value += variation;
    } else{
        return temp_value -= variation;
    }
}

EVENT_RESOURCE(
    temperature_sensor,
    "title=\"Temperature sensor\";obs",
    get_temp_handler,
    NULL,
    NULL,
    NULL,
    temp_event_handler
);

static void get_temp_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

    /* converting temperature from float to string*/
    char payload[30];
    sprintf(payload, "{\"temp\":%.2f}", temp_value);

    printf("payload: %s, length of payload: %d\n", payload, strlen(payload));

    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_payload(response, payload, strlen(payload));
}

static void temp_event_handler(void){

    float sensed_temp = simulate_temp_sensor();
    printf("Sensed temperature: %.2f C\n", temp_value);

    if (sensed_temp < temp_threshold){
        printf("Sensed temperature below threshold\n");
    } else{
        printf("DANGER: sensed temperature above threshold\n");
        // when the temperature value rises above a certain threshold, notification is sent to observers
        coap_notify_observers( &temperature_sensor);   
    }
}