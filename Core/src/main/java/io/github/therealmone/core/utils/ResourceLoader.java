package io.github.therealmone.core.utils;

import com.opencsv.CSVReader;
import io.github.therealmone.core.beans.CommandBean;
import io.github.therealmone.core.beans.Lexeme;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ResourceLoader {
    private static Caster caster;
    private static Set<Lexeme> lexemes;
    private static Map<Integer, String[]> langRules;
    private static Map<String, Map<String, Integer>> analyzeTable;
    private static Set<CommandBean> commands;
    private static boolean loaded = false;

    static {
        caster = new Caster();
    }

    public synchronized static void initialize() {
        if(!loaded) {
            Thread current = Thread.currentThread();

            try {
                loadLexemes(current.getContextClassLoader().getResourceAsStream("lexemes.xml"));
                loadLangRules(current.getContextClassLoader().getResourceAsStream("langRules.csv"));
                loadAnalyzeTable(current.getContextClassLoader().getResourceAsStream("analyzeTable.csv"));
                loadCommands(current.getContextClassLoader().getResourceAsStream("commands.xml"));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            loaded = true;
        }
    }

    private static void loadLexemes(final InputStream is) throws Throwable {
        lexemes = new HashSet<>();

        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("lexemes_list/lexeme", Lexeme.class);
        digester.addRule("lexemes_list/lexeme", new LexemeRule());
        digester.addRule("lexemes_list/lexeme/type", new LexemeTypeRule());
        digester.addRule("lexemes_list/lexeme/template", new LexemeTemplateRule());
        digester.parse(is);
    }

    private static void loadLangRules(final InputStream is) {
        langRules = new HashMap<>();

        try {
            CSVReader csvReader = new CSVReader(new InputStreamReader(is));
            String[] nextLine;
            csvReader.readNext();

            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    langRules.put(caster.castToInt(nextLine[0].trim()), nextLine[2].split("\\s\\+\\s"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            csvReader.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadAnalyzeTable(final InputStream is) {
        analyzeTable = new HashMap<>();

        try {
            CSVReader csvReader = new CSVReader(new InputStreamReader(is));
            String[] description = csvReader.readNext();
            String[] nextLine;

            while ((nextLine = csvReader.readNext()) != null) {
                Map<String, Integer> tmp = new HashMap<>();
                for (int i = 1; i < nextLine.length; i++) {
                    tmp.put(description[i], caster.castToInt(nextLine[i]));
                }
                analyzeTable.put(nextLine[0], tmp);
            }

            csvReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadCommands(final InputStream is) throws Throwable {
        commands = new HashSet<>();

        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("commands/command", CommandBean.class);
        digester.addRule("commands/command", new CommandRule());
        digester.addRule("commands/command/type", new CommandTypeRule());
        digester.addRule("commands/command/template", new CommandTemplateRule());
        digester.parse(is);
    }

    public synchronized static Set<Lexeme> getLexemes() {
        return new HashSet<>(lexemes);
    }

    public synchronized static Map<Integer, String[]> getLangRules() {
        return new HashMap<>(langRules);
    }

    public synchronized static Map<String, Map<String, Integer>> getAnalyzeTable() {
        return new HashMap<>(analyzeTable);
    }

    public synchronized static Set<CommandBean> getCommands() {
        return new HashSet<>(commands);
    }

    private static class LexemeRule extends Rule {
        @Override
        public void begin(String namespace, String name, Attributes attributes) throws Exception {
            Lexeme lexeme = getDigester().peek();
            int priority = caster.castToInt(attributes.getValue("priority"));
            lexeme.setPriority(priority);
        }

        @Override
        public void end(String namespace, String name) throws Exception {
            lexemes.add(getDigester().peek());
        }
    }

    private static class LexemeTypeRule extends Rule {
        @Override
        public void body(String namespace, String name, String text) throws Exception {
            Lexeme lexeme = getDigester().peek();
            lexeme.setType(text);
        }
    }

    private static class LexemeTemplateRule extends Rule {
        @Override
        public void body(String namespace, String name, String text) throws Exception {
            Lexeme lexeme = getDigester().peek();
            lexeme.setPattern(Pattern.compile(text));
        }
    }

    private static class CommandRule extends Rule {
        @Override
        public void end(String namespace, String name) throws Exception {
            commands.add(getDigester().peek());
        }
    }

    private static class CommandTypeRule extends Rule {
        @Override
        public void body(String namespace, String name, String text) throws Exception {
            CommandBean command = getDigester().peek();
            command.setType(text);
        }
    }

    private static class CommandTemplateRule extends Rule {
        @Override
        public void body(String namespace, String name, String text) throws Exception {
            CommandBean command = getDigester().peek();
            command.setPattern(Pattern.compile(text));
        }
    }
}