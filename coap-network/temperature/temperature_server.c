#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "project-conf.h"
#include "contiki.h"

#include "routing/routing.h"

#include "coap-engine.h"

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

// variables to understand if the node is connected and registered
static bool connected = false;
static bool registered = false;

// timer to wait before connection and registration
static struct etimer wait_connection;
static struct etimer wait_registration;

static void check_router_reachable(){
    
    if (!NETSTACK_ROUTING.node_is_reachable()){
        
        LOG_INFO("Border router is not reachable!");
        etimer_reset( &wait_connection);

    } else{
        
        LOG_INFO("Border router is reachable!");
        connected = true;

    }
    
    
}

PROCESS(temperature_server, "Temperature sensor server");
AUTOSTART_PROCESSES(&temperature_server);

PROCESS_THREAD(temperature_server, ev, data){

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    PROCESS_BEGIN();

    etimer_set( &wait_connection, 1);

    while (!connected)
    {
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_connection));
        check_router_reachable();
    }

    LOG_INFO("Temperature senros is now connected!");

    while (!registered)
    {
        // TODO: write the code to send the registration request to registration server
    }
    

    PROCESS_END();
}


