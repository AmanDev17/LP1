import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Pass1 {
    int lc = 0;
    int libtab_ptr = 0, pooltab_ptr = 0;
    int symIndex = 0, litIndex = 0;
    LinkedHashMap<String, TableRow> SYMTAB;
    ArrayList<TableRow> LITTAB;
    ArrayList<Integer> POOLTAB;
    private BufferedReader br;

    public Pass1() {
        SYMTAB = new LinkedHashMap<>();
        LITTAB = new ArrayList<>();
        POOLTAB = new ArrayList<>();
        lc = 0;
        POOLTAB.add(0);
    }

    public static void main(String[] args) {
        Pass1 one = new Pass1();
        try {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            one.parseFile();
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
    }

    public void parseFile() throws Exception {
        String prev = "";
        String line, code;

        br = new BufferedReader(new FileReader("sample.asm"));  // Make sure sample.asm exists here or provide full path
        BufferedWriter bw = new BufferedWriter(new FileWriter("IC.txt"));
        INSTtable lookup = new INSTtable();

        while ((line = br.readLine()) != null) {
            String parts[] = line.trim().split("\\s+");
            if (parts.length == 0) continue;  // skip empty lines

            // Check parts length before accessing parts[i]
            if (parts.length >= 1 && !parts[0].isEmpty()) { // processing label
                if (SYMTAB.containsKey(parts[0]))
                    SYMTAB.put(parts[0], new TableRow(parts[0], lc, SYMTAB.get(parts[0]).getIndex()));
                else
                    SYMTAB.put(parts[0], new TableRow(parts[0], lc, ++symIndex));
            }

            if (parts.length > 1) {
                String op = parts[1];

                if (op.equals("LTORG")) {
                    int ptr = POOLTAB.get(pooltab_ptr);
                    for (int j = ptr; j < libtab_ptr; j++) {
                        lc++;
                        LITTAB.set(j, new TableRow(LITTAB.get(j).getSymbol(), lc));
                        code = "(DL,01)\t(C," + LITTAB.get(j).symbol + ")";
                        bw.write(code + "\n");
                    }
                    pooltab_ptr++;
                    POOLTAB.add(libtab_ptr);
                } else if (op.equals("START")) {
                    lc = expr(parts[2]);
                    code = "(AD,01)\t(C," + lc + ")";
                    bw.write(code + "\n");
                    prev = "START";
                } else if (op.equals("ORIGIN")) {
                    lc = expr(parts[2]);
                    String splits[] = parts[2].split("\\+");
                    code = "(AD,03)\t(S," + SYMTAB.get(splits[0]).getIndex() + ")+" + Integer.parseInt(splits[1]);
                    bw.write(code + "\n");
                } else if (op.equals("EQU")) {
                    int loc = expr(parts[2]);
                    if (parts[2].contains("+")) {
                        String splits[] = parts[2].split("\\+");
                        code = "(AD,04)\t(S," + SYMTAB.get(splits[0]).getIndex() + ")+" + Integer.parseInt(splits[1]);
                    } else if (parts[2].contains("-")) {
                        String splits[] = parts[2].split("\\-");
                        code = "(AD,04)\t(S," + SYMTAB.get(splits[0]).getIndex() + ")-" + Integer.parseInt(splits[1]);
                    } else {
                        code = "(AD,04)\t(C," + Integer.parseInt(parts[2]) + ")";
                    }
                    bw.write(code + "\n");
                    if (SYMTAB.containsKey(parts[0]))
                        SYMTAB.put(parts[0], new TableRow(parts[0], loc, SYMTAB.get(parts[0]).getIndex()));
                    else
                        SYMTAB.put(parts[0], new TableRow(parts[0], loc, ++symIndex));
                } else if (op.equals("DC")) {
                    lc++;
                    int constant = Integer.parseInt(parts[2].replace("'", ""));
                    code = "(DL,01)\t(C," + constant + ")";
                    bw.write(code + "\n");
                } else if (op.equals("DS")) {
                    int size = Integer.parseInt(parts[2].replace("'", ""));
                    code = "(DL,02)\t(C," + size + ")";
                    bw.write(code + "\n");
                    lc = lc + size;
                    prev = "";
                } else if (lookup.getType(op).equals("IS")) {
                    code = "(IS,0" + lookup.getCode(op) + ")\t";
                    int j = 2;
                    String code2 = "";
                    while (j < parts.length) {
                        parts[j] = parts[j].replace(",", "");
                        if (lookup.getType(parts[j]).equals("RG")) {
                            code2 += lookup.getCode(parts[j]) + "\t";
                        } else {
                            if (parts[j].contains("=")) {
                                parts[j] = parts[j].replace("=", "").replace("'", "");
                                LITTAB.add(new TableRow(parts[j], -1, ++litIndex));
                                libtab_ptr++;
                                code2 += "(L," + (litIndex) + ")";
                            } else if (SYMTAB.containsKey(parts[j])) {
                                int ind = SYMTAB.get(parts[j]).getIndex();
                                code2 += "(S,0" + ind + ")";
                            } else {
                                SYMTAB.put(parts[j], new TableRow(parts[j], -1, ++symIndex));
                                int ind = SYMTAB.get(parts[j]).getIndex();
                                code2 += "(S,0" + ind + ")";
                            }
                        }
                        j++;
                    }
                    lc++;
                    code = code + code2;
                    bw.write(code + "\n");
                } else if (op.equals("END")) {
                    int ptr = POOLTAB.get(pooltab_ptr);
                    for (int j = ptr; j < libtab_ptr; j++) {
                        lc++;
                        LITTAB.set(j, new TableRow(LITTAB.get(j).getSymbol(), lc));
                        code = "(DL,01)\t(C," + LITTAB.get(j).symbol + ")";
                        bw.write(code + "\n");
                    }
                    pooltab_ptr++;
                    POOLTAB.add(libtab_ptr);
                    code = "(AD,02)";
                    bw.write(code + "\n");
                }
            }
        }
        bw.close();
        printSYMTAB();
        PrintLITTAB();
        printPOOLTAB();
    }

    void PrintLITTAB() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("LITTAB.txt"));
        System.out.println("\nLiteral Table\n");
        for (int i = 0; i < LITTAB.size(); i++) {
            TableRow row = LITTAB.get(i);
            System.out.println(i + "\t" + row.getSymbol() + "\t" + row.getAddess());
            bw.write((i + 1) + "\t" + row.getSymbol() + "\t" + row.getAddess() + "\n");
        }
        bw.close();
    }

    void printPOOLTAB() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("POOLTAB.txt"));
        System.out.println("\nPOOLTAB");
        System.out.println("Index\t#first");
        for (int i = 0; i < POOLTAB.size(); i++) {
            System.out.println(i + "\t" + POOLTAB.get(i));
            bw.write((i + 1) + "\t" + POOLTAB.get(i) + "\n");
        }
        bw.close();
    }

    void printSYMTAB() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("SYMTAB.txt"));
        System.out.println("SYMBOL TABLE");
        for (String key : SYMTAB.keySet()) {
            TableRow value = SYMTAB.get(key);
            System.out.println(value.getIndex() + "\t" + value.getSymbol() + "\t" + value.getAddess());
            bw.write(value.getIndex() + "\t" + value.getSymbol() + "\t" + value.getAddess() + "\n");
        }
        bw.close();
    }

    public int expr(String str) {
        int temp = 0;
        if (str.contains("+")) {
            String splits[] = str.split("\\+");
            temp = SYMTAB.get(splits[0]).getAddess() + Integer.parseInt(splits[1]);
        } else if (str.contains("-")) {
            String splits[] = str.split("\\-");
            temp = SYMTAB.get(splits[0]).getAddess() - (Integer.parseInt(splits[1]));
        } else {
            temp = Integer.parseInt(str);
        }
        return temp;
    }
}

