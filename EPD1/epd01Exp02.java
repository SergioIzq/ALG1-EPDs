public class epd01Exp02 {
    public static void main(String[] args) {
        System.out.println(puzzle(100000, 100));
    }

    public static int puzzle(int n, int count) {
        System.out.println("Paso número " + count);

        if (count > 0){
            
            if (n == 1) {
                return 1;
            }
            
            if (n % 2 == 0) {
                return puzzle(n / 2, count-1);
            } else {
                return puzzle(3 * n + 1, count-1);
            }
        }

        return 0;
    }
}
