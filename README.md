# Industrial Health Monitor
Industrial Health Monitor is an application that support the monitoring phase of in-
dustrial machinery. It creates a network of sensors and actuators that respectively monitor
the workflow and take actions to keep the piece of machinery in good working condition.
It exploits sensors that collect data about Temperature and Vibration of the machine
where they are applied and they send them to a Collector application that stores them
into a MySQL database. On the other end an actuator is responsible for enabling a Cool-
ing system when the temperature threshold, chosen by user, is overcame.
Collected data can be useful to create Machine Learning models capable of predict-
ing machine failures related to Vibration and Temperature. In this way the application
can help in organizing predicted maintenance work, that minimizes downtime, improves
efficiency and reduces costs.

# Features
*	Real-time monitoring of industrial machinery using temperature and vibration sensors
*	Automatic overheating detection and management
