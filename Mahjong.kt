/**
 * Created by muchuanxin on 2022-07-09
 */
fun main() {
    require(convertInputAndOutput("223344-234-2234") == "--25")
    require(convertInputAndOutput("1112345678999") == "123456789")
    require(convertInputAndOutput("23456-22233-789") == "147")
    require(convertInputAndOutput("123456-23444-55") == "-14-5")
    require(convertInputAndOutput("1112378999-123--") == "1469")
    require(convertInputAndOutput("2344445688999") == "178")
    require(convertInputAndOutput("223344-234-223-1") == "--12345")
    require(convertInputAndOutput("1234489-147-1234") == "null")
    require(convertInputAndOutput("123456789---1234") == "all")
}

fun convertInputAndOutput(input: String): String {
    val convertInput = mutableListOf<Mahjong>()
    input.split("-").forEachIndexed { index, s ->
        s.forEach { c ->
            Mahjong.parse(index * 10 + c.digitToInt())?.also {
                convertInput.add(it)
            } ?: throw IllegalArgumentException()
        }
    }
    val tingCards = whichCardToTing(convertInput)
    val wan = StringBuilder()
    val tong = StringBuilder()
    val tiao = StringBuilder()
    tingCards.forEach {
        when {
            it.value < 10 -> wan.append(it.value)
            it.value < 20 -> tong.append(it.value - 10)
            else -> tiao.append(it.value - 20)
        }
    }
    if (tong.isNotEmpty() || tiao.isNotEmpty()) {
        wan.append("-").append(tong)
    }
    if (tiao.isNotEmpty()) {
        wan.append("-").append(tiao)
    }
    if (wan.isEmpty()) {
        wan.append("null")
    }
    if (wan.length == 29) {
        wan.clear().append("all")
    }
    return wan.toString()
}

/**
 * @return 返回听的牌，empty表示没有听牌
 */
fun whichCardToTing(handCards: List<Mahjong>): List<Mahjong> {
    if (handCards.size != 13)
        return emptyList()
    // 过滤万能牌
    val remainCards = handCards.filter { it.isOmnipotentCard().not() }
    // 统计万能牌个数
    val omnipotentCount = handCards.count { it.isOmnipotentCard() }
    val counts = IntArray(31)
    remainCards.forEach {
        counts[it.value]++
    }
    val results = mutableListOf<Mahjong>()
    outer@
    for (tingCard in Mahjong.values().filter { it.isOmnipotentCard().not() }) {
        if (worthNotToTing(counts, tingCard.value, omnipotentCount))
            continue
        val tingCounts = counts.copyOf().apply { this[tingCard.value]++ }
        for (index in tingCounts.indices) {
            // 扣除单个对子
            var reduceCount: Int
            if (tingCounts[index] + omnipotentCount >= 2) {
                val cards = tingCounts.copyOf().apply {
                    // 如果个数不够2，用万能牌凑数
                    reduceCount = (2 - this[index]).coerceAtLeast(0)
                    this[index] = (this[index] - 2).coerceAtLeast(0)
                }
                if (judgeRemainAllKeAndShun(cards, omnipotentCount - reduceCount)) {
                    results.add(tingCard)
                    continue@outer
                }
            }
        }
    }
    return results
}

/**
 * 判断是否值得听的牌
 * @return true不值得听
 */
fun worthNotToTing(counts: IntArray, tingCard: Int, omnipotentCount: Int): Boolean {
    return counts[tingCard] + counts[tingCard - 1] + counts[tingCard + 1] + omnipotentCount == 0 || counts[tingCard] == 4
}

/**
 * 判断剩余牌是否全是刻和顺
 */
fun judgeRemainAllKeAndShun(counts: IntArray, omnipotentCount: Int): Boolean {
    var remainOmnipotent = omnipotentCount
    for (index in 0..counts.lastIndex - 2) {
        if (counts[index] <= 0) continue
        when (counts[index]) {
            2 -> {
                if ((counts[index + 1] - 2).coerceAtMost(0) + (counts[index + 2] - 2).coerceAtMost(0) < -1 && remainOmnipotent >= 1) {
                    remainOmnipotent--
                } else if ((counts[index + 1] - 2).coerceAtMost(0) + (counts[index + 2] - 2).coerceAtMost(0) + remainOmnipotent < 0) {
                    return false
                } else {
                    counts[index + 1] -= 2
                    counts[index + 2] -= 2
                    remainOmnipotent += ((counts[index + 1].coerceAtMost(0)) + (counts[index + 2].coerceAtMost(0)))
                }
            }
            1, 4 -> {
                if ((counts[index + 1] - 1).coerceAtMost(0) + (counts[index + 2] - 1).coerceAtMost(0) + remainOmnipotent < 0) {
                    return false
                }
                counts[index + 1] -= 1
                counts[index + 2] -= 1
                remainOmnipotent += ((counts[index + 1].coerceAtMost(0)) + (counts[index + 2].coerceAtMost(0)))
            }
        }
    }
    return (counts[29] + remainOmnipotent) % 3 == 0
}

enum class Mahjong(val value: Int) {
    // 万1~9
    WAN1(1), WAN2(2), WAN3(3), WAN4(4), WAN5(5), WAN6(6), WAN7(7), WAN8(8), WAN9(9),

    // 筒1~9
    TONG1(11), TONG2(12), TONG3(13), TONG4(14), TONG5(15), TONG6(16), TONG7(17), TONG8(18), TONG9(19),

    // 条1~9
    TIAO1(21), TIAO2(22), TIAO3(23), TIAO4(24), TIAO5(25), TIAO6(26), TIAO7(27), TIAO8(28), TIAO9(29),

    // 万能牌，春夏秋冬梅兰竹菊
    SPRING(31), SUMMER(32), AUTUMN(33), WINTER(34), PLUM(35), ORCHID(36), BAMBOO(37), CHRYSANTHEMUM(38);

    fun isOmnipotentCard(): Boolean {
        return this in listOf(SPRING, SUMMER, AUTUMN, WINTER, PLUM, ORCHID, BAMBOO, CHRYSANTHEMUM)
    }

    companion object {
        fun parse(value: Int): Mahjong? {
            return values().find { it.value == value }
        }
    }
}
