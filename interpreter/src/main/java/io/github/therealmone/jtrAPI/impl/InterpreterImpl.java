package io.github.therealmone.jtrAPI.impl;

import io.github.therealmone.core.exceptions.TranslatorException;
import io.github.therealmone.core.IException;
import io.github.therealmone.core.utils.SavePrinter;
import io.github.therealmone.jtrAPI.Interpreter;
import io.github.therealmone.jtrAPI.LexerModule;
import io.github.therealmone.jtrAPI.ParserModule;
import io.github.therealmone.jtrAPI.StackMachineModule;
import io.github.therealmone.jtrAPI.utils.RPNOptimizer;
import io.github.therealmone.stackmachine.StackMachine;
import io.github.therealmone.tdf4j.commons.Stream;
import io.github.therealmone.tdf4j.commons.Token;
import io.github.therealmone.tdf4j.generator.impl.LexerGeneratorImpl;
import io.github.therealmone.tdf4j.generator.impl.ParserGeneratorImpl;
import io.github.therealmone.tdf4j.lexer.Lexer;
import io.github.therealmone.tdf4j.lexer.UnexpectedSymbolException;
import io.github.therealmone.tdf4j.parser.Parser;
import io.github.therealmone.tdf4j.parser.UnexpectedTokenException;
import io.github.therealmone.tdf4j.parser.model.ast.AST;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InterpreterImpl implements Interpreter {
    private final static Logger log = LogManager.getLogger(InterpreterImpl.class);
    private final Lexer lexer;
    private final Parser parser;
    private final ParserModule module;
    private final StackMachine stackMachine;
    private final RPNOptimizer optimizer;

    @Inject
    public InterpreterImpl(
            final LexerModule lexer,
            final ParserModule parser,
            final StackMachineModule stackMachine
    ) {
        this.lexer = new LexerGeneratorImpl().generate(lexer);
        this.parser = new ParserGeneratorImpl().generate(parser);
        this.module = parser;
        this.stackMachine = StackMachine.newInstance(stackMachine);
        this.optimizer = new RPNOptimizer();
        log.debug("Parser : \n{}", this.parser.meta().sourceCode());
    }

    @Override
    public void process(final String program, final OutputStream output) {
        try {
            SavePrinter.setOut(output);
            log.debug("Program : \n{}", program);
            final StreamDecorator<Token> stream = new StreamDecorator<>(lexer.stream(program));
            final AST ast = parser.parse(stream);
            log.debug("AST : \n{}", ast);
            log.debug("RPN : \n{}", module.getRPN());
            stackMachine.process(optimizer.optimize(module.getRPN()));
        } catch (TranslatorException | UnexpectedTokenException | UnexpectedSymbolException e) {
            if (e instanceof IException) {
                ((IException) e).message();
            } else {
                SavePrinter.savePrintln(e.getMessage());
            }
        }
    }

    private class StreamDecorator<T> implements Stream<T> {
        private final Stream<T> stream;

        StreamDecorator(final Stream<T> stream) {
            this.stream = stream;
        }

        @Override
        public Supplier<T> generator() {
            return stream.generator();
        }

        @Nullable
        @Override
        public T next() {
            final T next = stream.next();
            log.debug("Next token : {}", next);
            return next;
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            T value;
            while((value = this.next()) != null) {
                action.accept(value);
            }
        }
    }
}
