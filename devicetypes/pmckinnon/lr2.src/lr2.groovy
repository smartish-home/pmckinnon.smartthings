preferences {
}

metadata {
    definition (name: "LR2 Controller", namespace: "pmckinnon", author: "pmckinnon@ojolabs.com") {
        capability "Actuator"
        capability "Switch"
        capability "Polling"
        capability "Switch Level"

        attribute "mute", "string"
        attribute "input", "string"

        command "mute"
        command "unmute"
        command "movieMode"
        command "musicMode"
        command "garageMusicMode"
        command "gameMode"
        command "zoneOneOn"
        command "zoneOneOff"
        command "zoneTwoOn"
        command "zoneTwoOff"

        command "autoLightingOn"
        command "autoLightingOff"
    }

    simulator {
        // TODO-: define status and reply messages here
    }

    tiles(scale: 2) {
        def white = '#ffffff'
        def red = '#F46F7D'
        def green = '#B6F7BD'
        def blue = '#BAEBFB'

        standardTile("main", "device.switch", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: '${name}', action:"switch.off", backgroundColor: "#79b821", icon:"st.Electronics.electronics13"
            state "off", label: '${name}', action:"switch.on", backgroundColor: "#ffffff", icon:"st.Electronics.electronics13"
        }

        valueTile("modeTitle", "device.modeTitle", width: 2, height: 1) {
            state "default", label: 'Mode'
        }
        standardTile("movie", "device.switchMovie", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: 'Movies', backgroundColor: green, icon:"st.Electronics.electronics3"
            state "off", label: 'Movies', action:"movieMode", backgroundColor: white, icon:"st.Electronics.electronics3"
        }
        standardTile("music", "device.switchMusic", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: 'Music', backgroundColor: green, icon:"st.Electronics.electronics12"
            state "off", label: 'Music', action:"musicMode", backgroundColor: white, icon:"st.Electronics.electronics12"
        }
        standardTile("game", "device.switchGame", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: 'Games', backgroundColor: green, icon:"st.Electronics.electronics5"
            state "off", label: 'Games', action:"gameMode", backgroundColor: white, icon:"st.Electronics.electronics5"
        }
        standardTile("garageMusic", "device.switchGarageMusic", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: 'Garage', backgroundColor: green, icon:"st.Electronics.electronics1"
            state "off", label: 'Garage', action:"garageMusicMode", backgroundColor: white, icon:"st.Electronics.electronics1"
        }

        valueTile("speakersTitle", "device.speakersTitle", width: 4, height: 1) {
            state "default", label: 'Speaker Select'
        }
        standardTile("speakersLR2", "device.switchZone1", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: 'LR2', action: "zoneOneOff", backgroundColor: blue, icon:"st.Electronics.electronics16"
            state "off", label: 'LR2', action:"zoneOneOn", backgroundColor: white, icon:"st.Electronics.electronics16"
        }
        standardTile("speakersGarage", "device.switchZone2", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: 'Garage', action: "zoneTwoOff", backgroundColor: blue, icon:"st.Electronics.electronics17"
            state "off", label: 'Garage', action:"zoneTwoOn", backgroundColor: white, icon:"st.Electronics.electronics17"
        }

        standardTile("mute", "device.mute", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "muted", label: '${name}', action:"unmute", backgroundColor: red, icon:"st.Electronics.electronics13"
            state "unmuted", label: '${name}', action:"mute", backgroundColor: white, icon:"st.Electronics.electronics13"
        }
        controlTile("level", "device.level", "slider", height: 1, width: 5, inactiveLabel: false, range: "(0..100)") {
            state "level", label: '${name}', action:"setLevel", backgroundColor: '#269DC5'
        }

        valueTile("lightModeTitle", "device.lightModeTitle", width: 2, height: 1) {
            state "default", label: 'Lighting'
        }
        standardTile("autoLighting", "device.switchAutoLighting", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false) {
            state "on", label: 'Auto', action: 'autoLightingOff', backgroundColor: green, icon:"st.Lighting.light11"
            state "off", label: 'Disabled', action:"autoLightingOn", backgroundColor: red, icon:"st.Lighting.light13"
        }

        main "main"
        details([
            "modeTitle", "movie", "music", "game", "garageMusic", "speakersTitle", "speakersLR2", "speakersGarage", "level", "mute",
            'lightModeTitle', 'autoLighting'
        ])
    }
}

def initialize() {
    log.debug "LR2 Initialize"
}

def installed() {
    log.debug "LR2 Installed"
}

def updated() {
    log.debug "LR2 Updated"
    mediaConfig(mode: "movie", level: 50)
}

def poll() {
    log.debug "LR2 poll"
}

def zoneOneOn() {
    mediaConfig(zone1: true)
}

def zoneOneOff() {
    mediaConfig(zone1: false)
}

def zoneTwoOn() {
    mediaConfig(zone2: true)
}

def zoneTwoOff() {
    mediaConfig(zone2: false)
}

def movieMode() {
    mediaConfig(mode: 'movie')
}

def musicMode() {
    mediaConfig(mode: 'music')
}

def garageMusicMode() {
    mediaConfig(mode: 'garageMusic')
}

def gameMode() {
    mediaConfig(mode: 'game')
}

def mediaConfig(config) {
    log.debug("mediaConfig($config)")
    def level = config['level']
    def mode = config['mode']
    def mute = config['mute']
    def zone1 = config['zone1']
    def zone2 = config['zone2']
    def input = null

    if(mode) {
        log.debug "Mode: $mode"
        sendEvent(name: 'switchMovie',        value: mode == 'movie'        ? 'on' : 'off')
        sendEvent(name: 'switchMusic',        value: mode == 'music'        ? 'on' : 'off')
        sendEvent(name: 'switchGame',         value: mode == 'game'         ? 'on' : 'off')
        sendEvent(name: 'switchGarageMusic',  value: mode == 'garageMusic'  ? 'on' : 'off')

        def inputMap = [
            movie: 'AV1',
            music: 'AUDIO1',
            game:  'AV2',
            garageMusic: 'AUDIO1'
        ]

        input = inputMap[mode]

        sendEvent(name: 'input', value: input)

        if(zone1 == null) {
            zone1 = (mode != 'garageMusic')
        }

        if(zone2 == null) {
            zone2 = (mode == 'garageMusic')
        }

        if(mute == null) {
            mute = false
        }
    }

    if(zone1 != null) {
        sendEvent(name: 'switchZone1', value: zone1 ? 'on': 'off')
    }

    if(zone2 != null) {
        sendEvent(name: 'switchZone2', value: zone2 ? 'on': 'off')
    }

    if(level != null) {
        sendEvent(name: 'level', value: level)
    }

    if(mute != null) {
        sendEvent(name: 'mute', value: mute ? 'muted' : 'unmuted')
    }

    sendEvent(name: 'mediaConfigured', value: [
        input: device.currentValue('input'),
        level: device.currentValue('level'),
        mute:  device.currentValue('mute'),
        zone1: device.currentValue('switchZone1'),
        zone2: device.currentValue('switchZone2')
    ])
}

def parse(String description) {
    log.debug "parse: $description"
}

def updateDevice() {
    log.debug "updateDevice()"
}

// Needs to round to the nearest 5
def setLevel(value) {
    log.debug "setLevel($value)"
    mediaConfig(level: value)
}

def on() {
    log.debug "on()"
}

def off() {
    log.debug "off()"
}


def toggleMute(){
    log.debug "toggle mute"
    (device.currentValue("mute") == "muted") ? unmute() : mute()
}

def mute() {
    log.debug "mute"
    mediaConfig(mute: true)
}

def unmute() {
    log.debug "unmute"
    mediaConfig(mute: false)
}

def autoLightingOn() {
    log.debug "autoLightingOn()"
    lightingConfig(automatic: true)
}

def autoLightingOff() {
    log.debug "autoLightingOff()"
    lightingConfig(automatic: false)
}

def lightingConfig(config) {
    sendEvent(name: 'switchAutoLighting', value: config.automatic ? 'on': 'off')
}
