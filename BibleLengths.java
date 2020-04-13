package Java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class BibleLengths {

    public static final boolean FULL_BIBLE_FLAG = true;
    public static final String PATH_PREFIX = "src/KJV/";
    public static final String PATH_SUFFIX = ".txt";
    public static final String WHOLE_BIBLE_PATH = "src/whole_bible.docx";
    public static final String WORD_COUNT_PATH = "src/wordcount.txt";
    public static long startTime;

    public static void main(String[] args) {
        setStartTime();

        clearFile(WORD_COUNT_PATH);
        if (FULL_BIBLE_FLAG) {
             clearFile(WHOLE_BIBLE_PATH);
        }
        int grandTotalWords = 0;
        int grandTotalCharacters = 0;
        for (int bookNumber = 1; bookNumber <= 66; ++bookNumber) {
            String bookPath = PATH_PREFIX + makeTwoDigits(bookNumber) + PATH_SUFFIX;
            List<String> lines = readFile(bookPath);
            if (FULL_BIBLE_FLAG) { appendBulkLines(WHOLE_BIBLE_PATH, lines); }
            List<List<String>> chapters = splitIntoChapters(lines);
            int[] wordCount = countWords(chapters);
            int[] characterCount = countCharacters(chapters);
            int[] verseCount = countVerses(chapters);
            grandTotalWords += addUp(wordCount);
            grandTotalCharacters += addUp(characterCount);
            for (int chapNum = 0; chapNum < chapters.size(); ++chapNum) {
                String write = generateString(bookNumber, chapNum + 1,
                        wordCount[chapNum], characterCount[chapNum], verseCount[chapNum]);
                appendLine(WORD_COUNT_PATH, write);
            }
        }

        System.out.println("Word Count = " + grandTotalWords);
        System.out.println("Character Count = " + grandTotalCharacters);

        printDeltaTime();
    }

    public static String makeTwoDigits(int num) {
        if (num < 10) {
            return "0" + num;
        }
        return "" + num;
    }

    public static String generateString(int book, int chapter, int wordCount, int characterCount, int verseCount) {
        return makeTwoDigits(book) + "\t" + makeTwoDigits(chapter) +
                "\t" + wordCount + "\t" + characterCount + "\t" + verseCount;
    }

    public static int addUp(int[] array) {
        int total = 0;
        for (int i : array) {
            total += i;
        }
        return total;
    }

    public static List<String> readFile(String filePath) {
        File file = new File(filePath);
        try {
            List<String> lines = Files.lines(Paths.get(file.toURI()))
                    .filter(line -> line != null && !line.equals(""))
                    .map(String::trim)
                    .map(BibleLengths::removeOddCharacters)
                    .collect(Collectors.toList());
            return lines;
        } catch (Exception e) {
            System.out.println("readFile error - " + e);
            return null;
        }
    }

    public static List<List<String>> splitIntoChapters(List<String> lines) {
        List<List<String>> chapters = new ArrayList<>();
        List<String> chapter = new ArrayList<>();
        chapter.add(lines.get(1));
        for (String line : lines.subList(2, lines.size())) {
            if (isNewChapter(line)) {
                chapters.add(chapter);
                chapter = new ArrayList<>();
            }
            chapter.add(line);
        }
        chapters.add(chapter);
        return chapters;
    }

    public static String removeOddCharacters(String s) {
        return s.replaceAll("[^!-~\\u20000-\\uFE1F\\uFF00-\\uFFEF] ", "");
    }

    public static void clearFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch(Exception e) {
            System.out.println("clearFile error - " + e);
        }
    }

    public static BufferedWriter getFileWriter(String filePath) {
        try {
            return new BufferedWriter(new FileWriter(filePath, true));
        }
        catch (Exception e) {
            System.out.println("getFileWriter Error - " + e);
            return null;
        }
    }

    public static boolean appendLine(BufferedWriter writer, String s, boolean newLineOnChapter) {
        if (s == null) {
            return false;
        }
        s = s.trim();
        if (s.length() == 0) {
            return false;
        }
        try {
            if (newLineOnChapter && isNewChapter(s)) {
                writer.append("\n");
            }
            writer.append(s).append("\n");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean appendLine(String path, String s) {
        BufferedWriter temporaryWriter = getFileWriter(path);
        if (temporaryWriter == null) {
            return false;
        }
        boolean response = appendLine(temporaryWriter, s, false);
        try {
            temporaryWriter.close();
        } catch (Exception e) {
            return false;
        }
        return response;
    }

    public static boolean appendBulkLines(String path, List<String> lines) {
        BufferedWriter temporaryWriter = getFileWriter(path);
        if (temporaryWriter == null) {
            return false;
        }
        boolean response = true;
        for (String line : lines) {
            response = appendLine(temporaryWriter, line, true) && response;
        }
        try {
            temporaryWriter.append("\n\n");
            temporaryWriter.close();
        } catch (Exception e) {
            return false;
        }
        return response;
    }

    public static int countCharacters(String s) {
        String[] line = s.trim().split(" ");
        int total = 0, i = 0;
        if (line.length <= 0) {
            return 0;
        }
        if (isNumeric(line[0])) {
            i++;
        }
        for (; i < line.length; ++i) {
            total += line[i].length();
        }
        return total;
    }

    public static int[] countCharacters(List<List<String>> chapters) {
        int[] characterCount = new int[chapters.size()];
        for (int i = 0; i < chapters.size(); ++i) {
            for (String line : chapters.get(i)) {
                characterCount[i] += countCharacters(line);
            }
        }
        return characterCount;
    }

    public static int countWords(String s) {
        String[] line = s.trim().split(" ");
        if (line.length <= 0) {
            return 0;
        }
        if (isNumeric(line[0])) {
            return line.length - 1;
        }
        return line.length;
    }

    public static int[] countWords(List<List<String>> chapters) {
        int[] wordCount = new int[chapters.size()];
        for (int i = 0; i < chapters.size(); ++i) {
            for (String line : chapters.get(i)) {
                wordCount[i] += countWords(line);
            }
        }
        return wordCount;
    }

    public static int[] countVerses(List<List<String>> chapters) {
        int[] verses = new int[chapters.size()];
        for (int i = 0; i < chapters.size(); ++i) {
            verses[i] = chapters.get(i).size() - 1;
        }
        return verses;
    }

    public static boolean isNumeric(String num) {
        try {
            Integer.parseInt(num);
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isNewChapter(String s) {
        String[] line = s.trim().split(" ");
        if (line[0].toLowerCase().equals("chapter") || line[0].toLowerCase().equals("psalm")) {
            try {
                Integer.parseInt(line[1]);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static void printChapters(List<List<String>> chapters) {
        for (List<String> chapter : chapters) {
            for (String verse : chapter) {
                System.out.println(verse);
            }
            System.out.println("");
        }
    }

    public static void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    public static void printDeltaTime() {
        System.out.println("took " + (System.currentTimeMillis() - startTime) + " ms");
    }
}
