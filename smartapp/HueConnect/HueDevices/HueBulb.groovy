/**
 *  Hue Bulb
 *
 *  Author: SmartThings
 */

preferences {
input("transitionTimePref", "integer", range: "(0..4)", required: false, title: "Time it takes for the lights to transition (default: 4)")   
section("Choose light effects...")
			{
				input "color", "enum", title: "Hue Color?", required: false, multiple:false, description: "Set the default colour (For when reset button is pressed)", options: [
					  "Soft White", "White", "Daylight", "Warm White", "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "lightLevel", "enum", title: "Light Level?", required: true, description: "Set the default Level (For when reset button is pressed)", options: ["10","20","30","40","50","60","70","80","90","100"]
			}
}


metadata {
    // Automatically generated. Make future change here.
    definition (name: "Hue Bulb", namespace: "smartthings", author: "SmartThings") {
        capability "Switch Level"
        capability "Actuator"
        capability "Color Control"
        capability "Color Temperature"
        capability "Switch"
        capability "Refresh"
        capability "Sensor"
	    capability "Health Check"

        command "setAdjustedColor"
        command "reset"
        command "refresh"
        command "effectColorloop"        
        command "effectNone" 
        command "alertBlink"
        command "alertPulse"
        command "alertNone"
        
        attribute "alertMode", "string"
        attribute "effectMode", "string"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles (scale: 2){
        multiAttributeTile(name:"rich-control", type: "lighting", width: 6, height: 6, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setAdjustedColor"
            }
        }
        
        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2000..6500)") {
        state "colorTemperature", action:"color temperature.setColorTemperature"
        }

        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        state "colorTemperature", label: 'WHITES'
        }

        standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"Reset", action:"reset", icon:"st.lights.philips.hue-single"
        }
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        standardTile("effectSelector", "device.effectMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "colorloop on", label:'${name}', icon:"st.Weather.weather3", action:"effectColorloop", nextState:"colorloop off"
            state "colorloop off", label:'${name}', icon:"st.Weather.weather3", action:"effectNone", nextState:"colorloop on"
		}
        
        standardTile("alertSelector", "device.alertMode", decoration: "flat", width: 2, height: 2) {
        	state "blink", label:'${name}', action:"alertBlink", icon:"st.Lighting.light11", backgroundColor:"#ffffff", nextState:"pulse"
            state "pulse", label:'${name}', action:"alertPulse", icon:"st.Lighting.light11", backgroundColor:"#e3eb00", nextState:"off"
            state "off", label:'${name}', action:"alertNone", icon:"st.Lighting.light13", backgroundColor:"#79b821", nextState:"blink"
       }
    }

    main(["rich-control"])
    details(["rich-control", "colorTempSliderControl", "colorTemp", "effectSelector", "alertSelector", "reset", "refresh"])
}

// parse events into attributes
def parse(description) {
    log.debug "parse() - $description"
    def results = []
    def map = description
    if (description instanceof String)  {
        log.debug "Hue Bulb stringToMap - ${map}"
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
    if (verifyPercent(percent)) {
	    log.trace parent.setLevel(this, percent)
    }

}

void setSaturation(percent) {
    log.debug "Executing 'setSaturation'"
    if (verifyPercent(percent)) {
	    log.trace parent.setSaturation(this, percent)
    }
 }

void setHue(percent) {
    log.debug "Executing 'setHue'"
    if (verifyPercent(percent)) {
	    log.trace parent.setHue(this, percent)
    }
}

void setColor(value) {
    log.debug "Executing 'setColor'"
    def events = []
    def validValues = [:]

    if (verifyPercent(value.hue)) {
        validValues.hue = value.hue
    }
    if (verifyPercent(value.saturation)) {
        validValues.saturation = value.saturation
    }
    if (value.hex != null) {
        if (value.hex ==~ /^\#([A-Fa-f0-9]){6}$/) {
            validValues.hex = value.hex
        } else {
            log.warn "$value.hex is not a valid color"
        }
    }
    if (verifyPercent(value.level)) {
        validValues.level = value.level
    }
    if (value.switch == "off" || (value.level != null && value.level <= 0)) {
        validValues.switch = "off"
    } else {
        validValues.switch = "on"
    }
    if (!validValues.isEmpty()) {
	    log.trace parent.setColor(this, validValues)
    }
}

void reset() {
    log.debug "Executing 'Reset'"
    def hueColor = "#FFD1AD"
	def saturation = 100
	switch(color) {
			case "Cool White":
				hueColor = "#DDE6FF"
				saturation = 13
				break;
			case "Daylight":
				hueColor = "#FFD1AD"
				saturation = 32
				break;
			case "White":
				hueColor = "#FFF8F7"
				saturation = 3
				break;
			case "Warm White":
				hueColor = "#FF8A1B"
				saturation = 89
				break;
	 	 	case "Blue":
				hueColor = "#1C05FF"
                saturation = 98
				break;
			case "Green":
				hueColor = "#03FF14"
                saturation = 98
				break;
			case "Yellow":
				hueColor = "#F9FF05"
                saturation = 98
				break;
			case "Orange":
				hueColor = "#FFAD24"
                saturation = 85
				break;
			case "Purple":
				hueColor = "#9113FF"
                saturation = 92
				break;
			case "Pink":
				hueColor = "#D812FF"
                saturation = 92
				break;
			case "Red":
				hueColor = "#FF0222"
                saturation = 99
				break;
	}
    def value = [level: lightLevel as Integer ?: 100, hex: hueColor, saturation: saturation, hue:23]
    setColor(value)
}

void setAdjustedColor(value) {
    if (value) {
        log.trace "setAdjustedColor: ${value}"
        def adjusted = value + [:]
        // Needed because color picker always sends 100
        adjusted.level = null
	    setColor(adjusted)
    } else {
        log.warn "Invalid color input $value"
    }
}

void setColorTemperature(value) {
    if (value) {
        log.debug "Executing 'setColorTemperature'"
        log.trace parent.setColorTemperature(this, value)
    } else {
        log.warn "Invalid color temperature $value"
    }
}

def verifyPercent(percent) {
    if (percent == null)
        return false
    else if (percent >= 0 && percent <= 100) {
        return true
    } else {
        log.warn "$percent is not 0-100"
        return false
    }
}

def ping() {
    log.trace "${parent.ping(this)}"
}

void setAlert(v) {
    log.debug "setAlert: ${v}, $this"
    parent.setAlert(this, v)
}

void alertNone() {
	log.debug "Alert option: 'none'"
    setAlert("none")
}

void alertBlink() {
	log.debug "Alert option: 'select'"
    setAlert("select")
}

void alertPulse() {
	log.debug "Alert option: 'lselect'"
    setAlert("lselect")
}

void setEffect(v) {
    log.debug "setEffect: ${v}, $this"
    parent.setEffect(this, v)
}

void effectNone() { 
    log.debug "Effect option: 'none'"
    setEffect("none")
}

void effectColorloop() { 
    log.debug "Effect option: 'colorloop'"
    setEffect("colorloop")
}
