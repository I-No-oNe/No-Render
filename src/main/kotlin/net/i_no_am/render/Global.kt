package net.i_no_am.render

interface Global {
    companion object {
        private const val CYAN = "§b"
        private const val RESET = "§r"
        const val PREFIX = RESET + "§7[" + CYAN  + "No Render§7] "
        const val RED = "§c"
        const val GREEN = "§a"
    }
}
