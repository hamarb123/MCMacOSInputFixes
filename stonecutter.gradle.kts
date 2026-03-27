plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") apply false
    id("net.fabricmc.fabric-loom-remap") apply false
}

stonecutter active "26.1"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod_version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
}
