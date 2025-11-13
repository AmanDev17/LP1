import java.io.*;

class Round {
    public static void main(String args[]) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int i, j, k, q, sum = 0;

        System.out.println("Enter number of processes:");
        int n = Integer.parseInt(in.readLine());

        int bt[] = new int[n];
        int wt[] = new int[n];
        int tat[] = new int[n];
        int a[] = new int[n];

        System.out.println("Enter Burst Time:");
        for (i = 0; i < n; i++) {
            System.out.println("Enter Burst Time for process " + (i + 1) + ":");
            bt[i] = Integer.parseInt(in.readLine());
        }

        System.out.println("Enter Time Quantum:");
        q = Integer.parseInt(in.readLine());

        for (i = 0; i < n; i++) {
            a[i] = bt[i];
        }

        for (i = 0; i < n; i++) {
            wt[i] = 0;
        }

        do {
            for (i = 0; i < n; i++) {
                if (bt[i] > q) {
                    bt[i] -= q;
                    for (j = 0; j < n; j++) {
                        if ((j != i) && (bt[j] != 0)) {
                            wt[j] += q;
                        }
                    }
                } else if (bt[i] > 0) {
                    for (j = 0; j < n; j++) {
                        if ((j != i) && (bt[j] != 0)) {
                            wt[j] += bt[i];
                        }
                    }
                    bt[i] = 0;
                }
            }

            sum = 0;
            for (k = 0; k < n; k++) {
                sum += bt[k];
            }
        } while (sum != 0);

        for (i = 0; i < n; i++) {
            tat[i] = wt[i] + a[i];
        }

        System.out.println("Process\t\tBT\tWT\tTAT");
        for (i = 0; i < n; i++) {
            System.out.println("Process " + (i + 1) + "\t" + a[i] + "\t" + wt[i] + "\t" + tat[i]);
        }

        float avg_wt = 0;
        float avg_tat = 0;
        for (j = 0; j < n; j++) {
            avg_wt += wt[j];
        }
        for (j = 0; j < n; j++) {
            avg_tat += tat[j];
        }

        System.out.println("Average Waiting Time: " + (avg_wt / n));
        System.out.println("Average Turnaround Time: " + (avg_tat / n));
    }
}
/*
Enter number of processes:
5
Enter Burst Time:
Enter Burst Time for process 1:
3
Enter Burst Time for process 2:
2
Enter Burst Time for process 3:
6
Enter Burst Time for process 4:
3
Enter Burst Time for process 5:
4
Enter Time Quantum:
3
*/
