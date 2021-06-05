package com.jakubeich.floydtriangle;

import java.util.Scanner;

class FloydTriangle
{
   public static void main(String args[])
   {
      int n, num = 1, c, d;
      Scanner in = new Scanner(System.in);
 
      System.out.println("Zadejte po�adovan� po�et ��dk� floydova troj�heln�ku: ");
      n = in.nextInt();
 
      System.out.println("Floyd�v troj�heln�k:");
 
      for ( c = 0 ; c <= n ; c++ )
      {
         for ( d = 0 ; d <= c ; d++ )
         {
            System.out.print(num+" ");
            num++;
         }
 
         System.out.println();
      }
   }
}

