//import java.io.*;
//import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int sum = 0;
        for (int i = 0; i < args.length; i++) {
            sum += Integer.parseInt(args[i]);
        }
        System.out.println(sum);
    }
}

//        Scanner scanner = new Scanner(System.in);
//        int a = scanner.nextInt();
//        int b = scanner.nextInt();
//        sum = a + b;