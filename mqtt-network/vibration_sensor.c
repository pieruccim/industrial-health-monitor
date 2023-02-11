/*---------------------------------------------------------------------------*/
#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"

#include <string.h>
#include <strings.h>
#include <time.h>
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "vibration-sensor"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Default config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)

/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    		    0
#define STATE_NET_OK    	    1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5

PROCESS_NAME(mqtt_vibration_client);
AUTOSTART_PROCESSES(&mqtt_vibration_client);

/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char broker_address[CONFIG_IP_ADDR_STR_LEN];
static char fractional_part[10];

// At the beginning vibration is 50 Hz

#define MAX_VIBRATION 150
#define MIN_VIBRATION 0

static float vibration_value = 50;


// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;

static struct mqtt_connection conn;

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_vibration_client, "Vibration sensor client");



/*---------------------------------------------------------------------------*/
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{
  LOG_INFO("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic,
          topic_len, chunk_len);
  return;
}
/*---------------------------------------------------------------------------*/


static void simulate_vibration_sensor(unsigned int i){

    /* Vibration vary randomly of a value in range [-10.0; 10.0] */
    float coeff = 10;

    srand(i);
    float variation = ((float)rand() / (float)(RAND_MAX) * coeff);

    srand(i);

    if(rand() % 2 == 0){
        vibration_value += variation;
    } else{
        vibration_value -= variation;
    }

    if (vibration_value < 0.1){

        vibration_value = 0.1;
    } else if (vibration_value > 150.1){

         vibration_value = 150.1;
    }
    
}


static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    LOG_INFO("Application has a MQTT connection\n");

    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    LOG_INFO("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

    state = STATE_DISCONNECTED;
    process_poll(&mqtt_vibration_client);
    break;
  }
  case MQTT_EVENT_PUBLISH: {
    msg_ptr = data;

    pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                msg_ptr->payload_chunk, msg_ptr->payload_length);
    break;
  }
  case MQTT_EVENT_SUBACK: {
#if MQTT_311
    mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

    if(suback_event->success) {
      LOG_INFO("Application is subscribed to topic successfully\n");
    } else {
      LOG_INFO("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
    LOG_INFO("Application is subscribed to topic successfully\n");
#endif
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    LOG_INFO("Application is unsubscribed to topic successfully\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    //LOG_INFO("Publishing complete.\n");
    break;
  }
  default:
    LOG_INFO("Application got a unhandled MQTT event: %i\n", event);
    break;
  }
}

static bool
have_connectivity(void)
{
  if(uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
     uip_ds6_defrt_choose() == NULL) {
    return false;
  }
  return true;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(mqtt_vibration_client, ev, data)
{

  PROCESS_BEGIN();
  
  LOG_INFO("MQTT Vibration Client process\n");

  // Initialize the ClientID as MAC address
  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  // Broker registration					 
  mqtt_register(&conn, &mqtt_vibration_client, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);
				  
  state=STATE_INIT;
				    
  // Initialize periodic timer to check the status 
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

  /* Main loop */
  while(1) {

    PROCESS_YIELD();

    if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) ||  ev == PROCESS_EVENT_POLL){
			  			  
        if(state==STATE_INIT){
            if(have_connectivity()==true)  
                state = STATE_NET_OK;
        } 
        
        if(state == STATE_NET_OK){

            // Connect to MQTT server
            LOG_INFO("Connecting...\n");
            
            memcpy(broker_address, broker_ip, strlen(broker_ip));
            mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,(DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND, MQTT_CLEAN_SESSION_ON);

            state = STATE_CONNECTING;
        }
        
        if(state==STATE_CONNECTED){
        
            // Publish something
            sprintf(pub_topic, "%s", "vibration");
            
            simulate_vibration_sensor(clock_seconds());

            sprintf(fractional_part, "%d", (int)((vibration_value - (int)vibration_value)*10));
            sprintf(app_buffer, "{\"vibration\": %d.%s, \"unit\": %s}", (int)vibration_value, fractional_part, "Hz");
            LOG_INFO("Publishing payload: %s\n", app_buffer);

            mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer, strlen(app_buffer), MQTT_QOS_LEVEL_1, MQTT_RETAIN_OFF);

        }else if ( state == STATE_DISCONNECTED ){

            LOG_ERR("Disconnected form MQTT broker\n");	

            // Recover from error
            mqtt_disconnect(&conn);

            /* If disconnection occurs the state is changed to STATE_INIT in this way a new connection attempt starts */
            state=STATE_INIT;
        }
        
        etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC * 4);
      
    }

  }

  PROCESS_END();
}
