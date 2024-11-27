package antifraud.security;

import java.util.Arrays;

public class LuhnAlgorithm {

    public static void main(String[] args) {}
    public static boolean isValidCreditCardNumber(String cardNumber) {
        // int array for processing the cardNumber
        int[] cardIntArray=new int[cardNumber.length()];

        for(int i=0;i<cardNumber.length();i++) {
            char c= cardNumber.charAt(i);
            cardIntArray[i]=  Integer.parseInt(String.valueOf(c));
        }

        for(int i=cardIntArray.length-2;i>=0;i=i-2) {
            int num = cardIntArray[i];
            num = num * 2;  // step 1
            if(num>9) {
                num = num%10 + num/10;  // step 2
            }
            cardIntArray[i]=num;
        }

        int sum = sumDigits(cardIntArray);  // step 3
        System.out.println(sum);
        // step 4
        return sum % 10 == 0;
    }

    public static int sumDigits(int[] arr) {
        return Arrays.stream(arr).sum();
    }
}
