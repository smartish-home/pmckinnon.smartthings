metadata {
	// Automatically generated. Make future change here.
	definition (name: "MIMOlite Bridge", namespace: "pmckinnon", author: "Patrick McKinnon") {
		capability "Configuration"
		capability "Polling"
		capability "Switch"
		capability "Refresh"
		capability "Contact Sensor"

		attribute "powered", "string"

		command "on"
		command "off"

    //zw:L type:1000 mfr:0084 prod:0453 model:0111 ver:1.17 zwv:3.42 lib:06 cc:72,86,71,30,31,35,70,85,25
    fingerprint deviceId: "0x1000", manufacturer: "0x0084", model: "0x0111", inClusters: "0x72,0x86,0x71,0x30,0x31,0x35,0x70,0x85,0x25"
	}

  preferences {
    input name: "outputMs", type: "number", title: "Momentary/Latched Output", description: "100-25500: Momentary, 0: Latched", range: "0..25500", defaultValue: 0, required: true
  }

	simulator {
	// Simulator stuff

	}

	// UI tile definitions
	tiles(scale: 2) {
    standardTile("switch", "device.switch", width: 4, height: 4, canChangeIcon: true) {
      state "on", label: "On", action: "off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
      state "off", label: "Off", action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    }

    standardTile("contact", "device.contact", inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
		}

    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

    standardTile("powered", "device.powered", inactiveLabel: false) {
			state "powerOn", label: "Power On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "powerOff", label: "Power Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
		}

		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main (["switch", "contact"])
		details(["switch", "powered", "refresh", "configure"])
	}
}

def parse(String description) {
  log.debug "description is: ${description}"

	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1])

  log.debug "command $cmd"

  if(cmd) {
    log.debug "command value is: $cmd.CMD"

    if (cmd.CMD == "7105") {				//Mimo sent a power loss report
      log.debug "Device lost power"
      sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power")
    } else {
      sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power")
    }

		result = createEvent(zwaveEvent(cmd))
	  log.debug "Parse returned ${result?.descriptionText}"
	}

	return result
}

def sensorValueEvent(Short value) {
	if (value) {
        sendEvent(name: "contact", value: "open")
        sendEvent(name: "switch", value: "doorOpen")
	} else {
        sendEvent(name: "contact", value: "closed")
        sendEvent(name: "switch", value: "doorClosed")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
  log.debug "value: " + cmd.value
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
  log.debug "Binary Report: $cmd"
  [name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
    log.debug "We lost power" //we caught this up in the parse method. This method not used.
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
  log.debug "Other command: $cmd"
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def installed() {
  log.debug "installed"
  configure();
}

def updated() {
  log.debug "updated"
  configure()
}

def configure() {
	log.debug "Configuring, outputMs: $outputMs" //setting up to monitor power alarm and actuator duration
  Integer config = outputMs == 0 ? 0 : (outputMs < 100 ? 1 : outputMs / 100);
  log.debug "Config: $config"
	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
    zwave.configurationV1.configurationSet(configurationValue: [config], parameterNumber: 11, size: 1).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 11).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def on() {
  log.debug "ON"
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def poll() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

def refresh() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}
