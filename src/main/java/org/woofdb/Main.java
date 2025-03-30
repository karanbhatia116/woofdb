package org.woofdb;


import org.woofdb.repl.Repl;

public class Main {
    public static void main(String[] args) {
        Repl repl = new Repl();
        repl.loop();
    }
}
