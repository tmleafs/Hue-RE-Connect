/**
 *  Hue Lux Bulb
 *
 *  Author: SmartThings
 */

preferences {
section("Choose light effects...")
			{
				input "lightLevel", "enum", title: "Light Level?", required: true, description: "Set the default Level (For when reset button is pressed)", options: ["10","20","30","40","50","60","70","80","90","100"]
			}
}

metadata {
    // Automatically generated. Make future change here.
    definition (name: "Hue Lux Bulb", namespace: "smartthings", author: "SmartThings") {
        capability "Switch Level"
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
        capability "Sensor"
	    capability "Health Check"    

        command "refresh"
        command "alertBlink"
        command "alertPulse"
        command "alertNone"
        command "reset"
        
        attribute "alertMode", "string"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"rich-control", type: "lighting", canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
              attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
              attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
              attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
              attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
              attributeState "level", action:"switch level.setLevel", range:"(0..100)"
            }
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
                attributeState "level", label: 'Level ${currentValue}%'
            }
        }

        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
            state "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
        }

        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "Reset", action: "reset", icon: "st.lights.philips.hue-single"
        }
        
        standardTile("alertSelector", "device.alertMode", decoration: "flat", width: 2, height: 2) {
        	state "blink", label:'${name}', action:"alertBlink", icon:"st.Lighting.light11", backgroundColor:"#ffffff", nextState:"pulse"
            state "pulse", label:'${name}', action:"alertPulse", icon:"st.Lighting.light11", backgroundColor:"#e3eb00", nextState:"off"
            state "off", label:'${name}', action:"alertNone", icon:"st.Lighting.light13", backgroundColor:"#79b821", nextState:"blink"
       }

        main(["switch"])
        details(["rich-control", "refresh", "alertSelector", "reset"])
    }
}

// parse events into attributes
def parse(description) {
    log.debug "parse() - $description"
    def results = []

    def map = description
    if (description instanceof String)  {
        log.debug "Hue Lux Bulb stringToMap - ${map}"
        map = stringToMap(description)
    }

    if (map?.name && map?.value) {
        results << createEvent(name: "${map?.name}", value: "${map?.value}")
    }
    results
}

// handle commands
void on() {
    log.trace parent.on(this)
}

void off() {
    log.trace parent.off(this)
}

void setLevel(percent) {
    log.debug "Executing 'setLevel'"
    if (percent != null && percent >= 0 && percent <= 100) {
        log.trace parent.setLevel(this, percent)
    } else {
        log.warn "$percent is not 0-100"
    }
}

def reset() {
    log.debug "Executing 'reset'"
    sendEvent(name: "level", value: lightLevel as Integer ?: 100)
    def value = lightLevel as Integer ?: 100
	parent.setLevel(this, value)
    log.debug "Reset Level to $value " 
}

void refresh() {
    log.debug "Executing 'refresh'"
    parent.manualRefresh()
}

def ping() {
    log.debug "${parent.ping(this)}"
}

def setAlert(v) {
    log.debug "setAlert: ${v}, $this"
    parent.setAlert(this, v)
    sendEvent(name: "alert", value: v, isStateChange: true)
}

def alertNone() {
	log.debug "Alert option: 'none'"
    setAlert("none")
}

def alertBlink() {
	log.debug "Alert option: 'select'"
    setAlert("select")
}

def alertPulse() {
	log.debug "Alert option: 'lselect'"
    setAlert("lselect")
}
