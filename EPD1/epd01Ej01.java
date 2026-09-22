public class epd01Ej01 {
    public static void main(String[] args) {
        int[] v = { 3, 5, 2, 4 };
        System.out.println(sumaCaramelos(v, v.length));
    }

    public static int sumaCaramelos(int v[], int n) {
        if (n > 0) {
            n = v[n-1] + sumaCaramelos(v, n-1);
        }

        return n;
    }
}
