import java.time.Instant;
class EasyComparableBasic {
	void autoCompare() {
		Instant i1 = Instant.now();
		Instant i2 = Instant.now();
		if (i1.compareTo(i2) < 0) {
			System.out.println("LT");
		} else if (i1.compareTo(i2) > 0) {
			System.out.println("GT");
		} else if (i1.compareTo(i2) == 0) {
			System.out.println("EQ");
		}
	}
	void classicCompare() {
		Long l1 = 1L;
		Long l2 = 2L;
		if (l1 < l2) {
			System.out.println("LT");
		} else if (l1 > l2) {
			System.out.println("GT");
		} else if (l1 == l2) {
			System.out.println("EQ");
		}
	}
}
