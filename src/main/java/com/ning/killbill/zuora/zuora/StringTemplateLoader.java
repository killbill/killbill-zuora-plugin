package com.ning.killbill.zuora.zuora;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

public class StringTemplateLoader {
    public static class StringTemplateBuilder {
        private final StringTemplate stringTemplate;

        public StringTemplateBuilder(StringTemplate stringTemplate) {
            this.stringTemplate = stringTemplate;
        }

        public StringTemplateBuilder define(String key, Object value) {
            stringTemplate.setAttribute(key, value);
            return this;
        }

        public String build() {
            return stringTemplate.toString();
        }
    }

    private static final String sep = "/"; // *Not* System.getProperty("file.separator"), which breaks in jars
    private final StringTemplateGroup group;

    public StringTemplateLoader(Class<?> baseClass) {
        String path = baseClass.getName().replaceAll("\\.", Matcher.quoteReplacement(sep)) + ".stg";

        try {
            InputStream ins = getClass().getResourceAsStream(path);

            if (ins == null) {
                // try with a slash in front
                ins = getClass().getResourceAsStream("/" + path);
                if (ins == null) {
                    throw new IllegalStateException("unable to find group file " + path + " on classpath");
                }
            }

            InputStreamReader reader = new InputStreamReader(ins);

            this.group = new StringTemplateGroup(reader, AngleBracketTemplateLexer.class);
            reader.close();
        }
        catch (IOException e) {
            throw new IllegalStateException("unable to load string template group " + path, e);
        }
    }

    public StringTemplateBuilder load(String name) {
        if (group.isDefined(name)) {
            return new StringTemplateBuilder(group.lookupTemplate(name).getInstanceOf());
        }
        else {
            throw new IllegalArgumentException("Did not find a template with the name " + name);
        }
    }
}
