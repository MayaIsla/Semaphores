import java.util.LinkedList;

import java.util.Queue;

import java.util.concurrent.Semaphore;

public class Customer implements Runnable {

  private int task,i, num;
  

  public static Semaphore max_customers = new Semaphore(5, true);
  public static Semaphore queue1notempty = new Semaphore(0, true);
  public static Semaphore queue2notempty = new Semaphore(0, true);
  public static Semaphore[] banktellerRequest = new Semaphore[] {
    new Semaphore(0),
    new Semaphore(0)
  };
  public static Semaphore[] depositReceipt = new Semaphore[] {
    new Semaphore(0),
    new Semaphore(0)
  };
  public static Semaphore[] depositComplete = new Semaphore[] {
    new Semaphore(0),
    new Semaphore(0)
  };
  public static Semaphore[] withdrawReceipt = new Semaphore[] {
    new Semaphore(0),
    new Semaphore(0)
  };
  public static Semaphore[] withdrawalComplete = new Semaphore[] {
    new Semaphore(0),
    new Semaphore(0)
  };
  public static Semaphore loanOfficerRequest = new Semaphore(0, true);
  public static Semaphore loanOfficerReceipt = new Semaphore(0, true);
  public static Semaphore loanTransactionComplete = new Semaphore(0, true);
  public static Semaphore[] tellerReady = new Semaphore[] {
    new Semaphore(0),
    new Semaphore(0),
    new Semaphore(0),
    new Semaphore(0),
    new Semaphore(0)
  };
  public static Semaphore[] loanOfficerReady = new Semaphore[] {
    new Semaphore(0),
    new Semaphore(0),
    new Semaphore(0),
    new Semaphore(0),
    new Semaphore(0)
  };

  public static Semaphore mutex1 = new Semaphore(1, true);
  public static Semaphore mutex2 = new Semaphore(1, true);
  public static int[] taskCustomer = new int[10];
  public static int[] tellerServingCustomer = new int[10];
  public static int[] deposit = new int[10];
  public static int[] withdraw = new int[10];
  public static int[] balance = new int[10];
  public static int[] loan = new int[10];
  public static int[] loanTotal = new int[10];
  public static Queue < Integer > queueBankTeller = new LinkedList < Integer > ();
  public static Queue < Integer > queueLoanOfficer = new LinkedList < Integer > ();

  Customer(int num)

  {

    this.num = num;

    balance[num] = 1000;

    loanTotal[num] = 0;

  }


  public void run()

  {

    for (i = 0; i < 3; i++)//thread number of clients
    {
      try
      {
        max_customers.acquire();
        task = assigntask();
        taskCustomer[num] = task;

        if (task == 1)

        {

          mutex1.acquire();
          queueBankTeller.add(num);
          queue1notempty.release();
          mutex1.release();
          tellerReady[num].acquire();
          deposit[num] = 100 * (1 + (int)(Math.random() * 5));
          System.out.println("Client " + num + " requests of teller " + tellerServingCustomer[num] + " to make a deposit of $" + deposit[num]);
          Thread.sleep(100);
          banktellerRequest[tellerServingCustomer[num]].release();
          depositReceipt[tellerServingCustomer[num]].acquire();
          Thread.sleep(100);
          System.out.println("Client " + num + " gets receipt from teller " + tellerServingCustomer[num]);
          depositComplete[tellerServingCustomer[num]].release();
        }

        if (task == 2)
        {

          mutex1.acquire();
          queueBankTeller.add(num);
          queue1notempty.release();
          mutex1.release();
          tellerReady[num].acquire();
          withdraw[num] = 100 * (1 + (int)(Math.random() * 5));
          System.out.println("Client " + num + " requests of teller " + tellerServingCustomer[num] + " to make a withdrawal of $" + withdraw[num]);
          Thread.sleep(100);
          banktellerRequest[tellerServingCustomer[num]].release();
          withdrawReceipt[tellerServingCustomer[num]].acquire();
          Thread.sleep(100);
          System.out.println("Client " + num + " gets cash and receipt from teller " + tellerServingCustomer[num]);
          withdrawalComplete[tellerServingCustomer[num]].release();
        }

        if (task == 3)

        {

          mutex2.acquire();
          queueLoanOfficer.add(num);
          queue2notempty.release();
          mutex2.release();
          loanOfficerReady[num].acquire();
          loan[num] = 100 * (1 + (int)(Math.random() * 5));
          System.out.println("Client " + num + " requests of Teller 3 to make a deposit of " + loan[num]);
          Thread.sleep(100);
          loanOfficerRequest.release();
          loanOfficerReceipt.acquire();
          Thread.sleep(100);
          System.out.println("Client " + num + " gets approval from Teller 3");
          loanTransactionComplete.release();
        }
        max_customers.release();
      }

      catch(InterruptedException e)

      {
        e.printStackTrace();
      }
    }

    System.out.println("Client " + num + " departs the bank");

  }


  private int assigntask()

  {
    int randNum;
    randNum = 1 + (int)(Math.random() * 3);
    return randNum;
  }

  public static void main(String args[])

  {

    int i = 0;
    final int NUMCUSTOMERS = 5; //try this out later
    int sumBalance = 0,
    sumLoanTotal = 0;
    LoanOfficer officer = new LoanOfficer();
    Thread myThread2 = new Thread();
    myThread2 = new Thread(officer);
    myThread2.setDaemon(true);
    myThread2.start();
    System.out.println("Teller 3 created ");

    Bankteller teller[] = new Bankteller[2];
    Thread myThread1[] = new Thread[2];

    for (i = 0; i < 2; ++i)

    {
      teller[i] = new Bankteller(i);
      myThread1[i] = new Thread(teller[i]);
      myThread1[i].setDaemon(true);
      myThread1[i].start();
      System.out.println("Teller " + i + " created ");
    }


    Customer cust[] = new Customer[NUMCUSTOMERS];
    Thread myThread[] = new Thread[NUMCUSTOMERS];

    for (i = 0; i < NUMCUSTOMERS; ++i)

    {

      cust[i] = new Customer(i);
      myThread[i] = new Thread(cust[i]);
      myThread[i].start();
      System.out.println("Customer " + i + " created ");

    }


    for (i = 0; i < NUMCUSTOMERS; ++i)
    {
      try
      {
        myThread[i].join();
        System.out.println("Customer " + i + " joined by main");
      }
      catch(InterruptedException e)
      {

}

    }

  

    //System.out.println("\n\t \n");

    //System.out.println("\t\tEnding Balance \tLoan Amount\n");

    for (i = 0; i < 5; i++)
    {
      //System.out.println("Customer " + i + "\t" + balance[i] + "\t\t" + loanTotal[i]);
      sumBalance = sumBalance + balance[i];
      sumLoanTotal = sumLoanTotal + loanTotal[i];
    }

   // System.out.println("\nTotals\t\t" + sumBalance + "\t\t" + sumLoanTotal);
  }
}

class Bankteller implements Runnable

{
  private int nextcustomer;
  private int nextcustomertask;
  private int num;

  Bankteller(int num)

  {
    this.num = num;
  }

  public void run()

  {
    while (true)
    {
      try
      {
        Customer.queue1notempty.acquire();
        Customer.mutex1.acquire();
        nextcustomer = Customer.queueBankTeller.remove();
        Customer.mutex1.release();
        nextcustomertask = Customer.taskCustomer[nextcustomer];
        Customer.tellerServingCustomer[nextcustomer] = num;
        System.out.println("Teller " + num + " Begins serving Customer " + nextcustomer);
        Customer.tellerReady[nextcustomer].release();
        if (nextcustomertask == 1)

        {

          Customer.banktellerRequest[num].acquire();
          System.out.println("Teller " + num + " processes deposit for Customer " + nextcustomer);
          Thread.sleep(400);
          Customer.balance[nextcustomer] = Customer.balance[nextcustomer] + Customer.deposit[nextcustomer];
          Customer.depositReceipt[num].release();
          Customer.depositComplete[num].acquire();
        }

        if (nextcustomertask == 2)
        {
          Customer.banktellerRequest[num].acquire();
          System.out.println("Teller " + num + " processes withdrawal for Customer " + nextcustomer);
          Thread.sleep(400);
          Customer.balance[nextcustomer] = Customer.balance[nextcustomer] - Customer.withdraw[nextcustomer];
          Customer.withdrawReceipt[num].release();
          Customer.withdrawalComplete[num].acquire();
        }
      }

      catch(InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}

class LoanOfficer implements Runnable
{
  private int nextcustomer;
  private int nextcustomertask;

  @Override

  public void run()
  {
    while (true)
    {
      try
      {

        Customer.queue2notempty.acquire();
        Customer.mutex2.acquire();
        nextcustomer = Customer.queueLoanOfficer.remove();
        Customer.mutex2.release();
        nextcustomertask = Customer.taskCustomer[nextcustomer];
        System.out.println("Teller 3 Begins serving Client " + nextcustomer);
        Customer.loanOfficerReady[nextcustomer].release();
        if (nextcustomertask == 3)
        {
          Customer.loanOfficerRequest.acquire();
          System.out.println("Teller approves withdral for Client " + nextcustomer);
          Thread.sleep(400);
          Customer.loanTotal[nextcustomer] = Customer.loanTotal[nextcustomer] + Customer.loan[nextcustomer];
          Customer.loanOfficerReceipt.release();
          Customer.loanTransactionComplete.acquire();
        }
      }
      catch(InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
