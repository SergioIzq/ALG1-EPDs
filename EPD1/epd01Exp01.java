public class epd01Exp01 {

    public static void main(String[] args) {
        System.out.println(fibonacci(3));
    }

    public static int fibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

}
