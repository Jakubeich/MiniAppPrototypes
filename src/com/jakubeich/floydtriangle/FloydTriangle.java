package com.jakubeich.floydtriangle;

import java.util.Scanner;

class FloydTriangle
{
   public static void main(String args[])
   {
      int n, num = 1, c, d;
      Scanner in = new Scanner(System.in);
 
      System.out.println("Zadejte požadovaný poèet øádkù floydova trojúhelníku: ");
      n = in.nextInt();
 
      System.out.println("Floydùv trojúhelník:");
 
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

