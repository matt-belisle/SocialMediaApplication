object ApplicationRegex {
    val hasTagRegex = """#([\w_-]+((##)[\w_-]+)*)""".toRegex()
    val hasMentionRegex = """\s@([\w_-]+)""".toRegex()
    val isSubTagRegex = """([\w_-]+)##[\w_-]+""".toRegex()
}