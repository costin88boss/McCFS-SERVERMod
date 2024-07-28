I may or may not have made this mod to mainly impress a girl (she rejected me ☹️)

either way, this fabric mod is meant to only be installed on a server.

it's meant to log ALMOST EVERYTHING that a player could do.

a bad design choice was to include the discord4j library AND its prerequisites within the mod jar itself (somehow, with the use of Gradle engineering). A better approach may have been to have a second, non-minecraft "relay" server that would uh, I guess interact with this mc mod using smth like HTTP or TCP using builtin java stuff?
