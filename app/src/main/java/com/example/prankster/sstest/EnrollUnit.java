//package com.example.prankster.sstest;
//
//import java.io.FileNotFoundException;
//import java.util.Arrays;
//import java.util.Scanner;
//import java.io.FileInputStream;
//
///**
// * Created by prankster on 2016-08-31.
// */
//
//public class EnrollUnit {
//    Scanner sc = new Scanner(new FileInputStream("input.txt"));
//
//    int TC;
//    int test_case;
//
//    TC = sc.nextInt();
//    for(test_case = 1; test_case <= TC; test_case++) {
//        // 이 부분에서 알고리즘 프로그램을 작성하십시오.
//        int ex = sc.nextInt();
//        int[] arr= new int[ex];
//        for(int i = 0; i<ex;i++){
//            arr[i] = sc.nextInt();
//            //System.out.println(arr[i]);
//        }
//
//        int result = 0;
//
//        Arrays.sort(arr);
//        for(int i=0 ; i<arr.length;i++){
//            result^=arr[i];
//
//        }
//        // 이 부분에서 정답을 출력하십시오.
//        System.out.println("Case #" + test_case);
//        System.out.println(result);
//    }
//
//    public EnrollUnit() throws FileNotFoundException {
//    }
//}
