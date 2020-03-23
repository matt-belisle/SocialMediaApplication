object ApplicationRegex {
    val hasTagRegex = """#([\w_-]+((##)[\w_-]+)*)""".toRegex()
    val hasMentionRegex = """\s@([\w_-]+)""".toRegex()
    val isSubtweetRegex = """([\w_-]+)##[\w_-]+""".toRegex()
}