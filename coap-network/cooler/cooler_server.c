#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "project-conf.h"

#include "contiki.h"
#include "sys/etimer.h"
#include "dev/leds.h"

#include "routing/routing.h"

#include "coap-engine.h"
#include "coap-blocking-api.h"

#include "sys/log.h"
#define LOG_MODULE "cooler-act"
#define LOG_LEVEL LOG_LEVEL_APP

#define SERVER_EP "coap://[fd00::1]:5683"
#define CONNECTION_TRY_INTERVAL 2
#define REGISTRATION_TRY_INTERVAL 1

#define SENSOR_NAME "cooler_actuator"

PROCESS(cooler_server, "Cooler actuator server");
AUTOSTART_PROCESSES(&cooler_server);

// variables to understand if the node is connected and registered
static bool connected = false;
static bool registered = false;

// timer to wait before connection and registration
static struct etimer wait_connection;
static struct etimer wait_registration;

char *registration_ep = "/registration"; 
char payload [50];
extern coap_resource_t cooler_actuator; // resource temperature_sensor in external file

static void check_router_reachable(){
    
    if (!NETSTACK_ROUTING.node_is_reachable()){
        
        LOG_INFO("Border router is not reachable!\n");
        etimer_reset( &wait_connection);

    } else{
        
        LOG_INFO("Border router is reachable!\n");
        leds_on(LEDS_NUM_TO_MASK(LEDS_LED2)); // yellow led on cooja
        connected = true;

    }   
}

void cooler_chunck_handler(coap_message_t *response){

    if(response == NULL){
        LOG_INFO("Registration request timed out! \n");
        etimer_set(&wait_registration, CLOCK_SECOND * REGISTRATION_TRY_INTERVAL);
        return;
    }

    const uint8_t *chunk;
    int len = coap_get_payload(response, &chunk);
    LOG_INFO("Response received: %.*s\n", len, (char *)chunk);

    if( strcmp((char *) chunk, "reg_completed") == 0){
        leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        registered = true;
    } else{
        etimer_set(&wait_registration, CLOCK_SECOND * REGISTRATION_TRY_INTERVAL);
    }

}

PROCESS_THREAD(cooler_server, ev, data){

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    PROCESS_BEGIN();

    etimer_set( &wait_connection, CLOCK_SECOND * CONNECTION_TRY_INTERVAL);

    while (!connected)
    {   
        // check if the sensor is connected through border router
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_connection));
        check_router_reachable();
    }

    
    LOG_INFO("Cooler actuator is now CONNECTED through border router!\n");

    while (!registered)
    {
        /* send the registration request to registration server */
        
        coap_endpoint_parse( SERVER_EP, strlen(SERVER_EP), &server_ep); // bind endpoint variable to endpoint string
        
        //prepare message to send
        coap_init_message( request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, registration_ep);

        sprintf(payload, "{\"device\":\"%s\"}", (char *)SENSOR_NAME);
        coap_set_payload(request, payload, strlen(payload));
        
        // send blocking request, handle response on handler function
        LOG_INFO("Blocking registration request is going to be sent with payload: %s of size %i\n", payload, strlen(payload));
        COAP_BLOCKING_REQUEST(&server_ep, request, cooler_chunck_handler);

        PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));
    }

    LOG_INFO("Cooler actuator is now REGISTERED to registration server!\n");
    LOG_INFO("Starting cooler server...\n");

    // activate the resource (endpoint of the resource is '/cooler_actuator')
    coap_activate_resource(&cooler_actuator, "cooler_actuator");

    PROCESS_END();
}


