package net.hamnaberg.json.codec;

import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import net.hamnaberg.json.Json;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class DecodeResult<A> {

    private DecodeResult() {
    }

    public final <B> DecodeResult<B> map(Function<A, B> f) {
        return fold(DecodeResult::fail, f.andThen(DecodeResult::ok));
    }

    public final DecodeResult<A> filter(Predicate<A> p) {
        return filter(p, () -> "Filter failed");
    }

    public final DecodeResult<A> filter(Predicate<A> p, Supplier<String> errorSupplier) {
        return flatMap(a -> p.test(a) ? ok(a) : fail(errorSupplier.get()));
    }

    public final void forEach(Consumer<A> f) {
        consume(ignore -> {}, f);
    }

    public final <B> DecodeResult<B> flatMap(Function<A, DecodeResult<B>> f) {
        return fold(DecodeResult::fail, f);
    }

    public final A getOrElse(Supplier<A> orElse) {
        return fold(ignore -> orElse.get(), Function.identity());
    }

    public abstract <X extends Throwable> A getOrElseThrow(Function<String, X> exProvider) throws X;

    public final A unsafeGet() {
        return fold(e -> {
            throw new NoSuchElementException(e);
        }, Function.identity());
    }

    public final Either<String, A> toEither() {
        return fold(Either::left, Either::right);
    }

    public final Option<A> toOption() {
        return fold(ignore -> Option.none(), Option::of);
    }

    public final Optional<A> toJavaOptional() {
        return toOption().toJavaOptional();
    }

    public abstract <B> B fold(Function<String, B> failFunction, Function<A, B> okFunction);

    public abstract void consume(Consumer<String> failFunction, Consumer<A> okFunction);

    public boolean isOk() {
        return fold(a -> false, a -> true);
    }

    public boolean isFailure() {
        return fold(a -> true, a -> false);
    }

    public static <A> DecodeResult<List<A>> sequence(List<DecodeResult<A>> decodeResults) {
        if (decodeResults.isEmpty()) {
            return DecodeResult.ok(List.empty());
        }
        final ArrayList<A> list = new ArrayList<>();
        final ArrayList<String> errors = new ArrayList<>();

        for (DecodeResult<A> res : decodeResults) {
            res.consume(errors::add, list::add);
        }
        if (errors.isEmpty()) {
            return DecodeResult.ok(List.ofAll(list));
        } else {
            return DecodeResult.fail("One or more results failed: " + List.ofAll(errors).mkString("\n"));
        }
    }

    public static <A> DecodeResult<A> ok(A value) {
        return new Ok<>(value);
    }

    public static <A> DecodeResult<A> fromOption(Option<A> value) {
        return fromOption(value, () -> "No value found");
    }

    public static <A> DecodeResult<A> fromOption(Option<A> value, Supplier<String> error) {
        return value.map(DecodeResult::ok).getOrElse(fail(error.get()));
    }

    @SuppressWarnings("unchecked")
    public static <A> DecodeResult<A> fail(String message) {
        return (DecodeResult<A>) new Failure(message);
    }


    private static DecodeResult<Json.JValue> getValue(Json.JObject object, String name) {
        return object.
                get(name).
                map(DecodeResult::ok).
                getOrElse(DecodeResult.fail(String.format("'%s' not found in %s", name, object.nospaces())));
    }

    public static <A> DecodeResult<A> decode(Json.JObject object, FieldDecoder<A> decoder) {
        return decode(object, decoder.name, decoder.decoder);
    }

    public static <A> DecodeResult<A> decode(Json.JObject object, String name, DecodeJson<A> decoder) {
        DecodeResult<A> result = getValue(object, name).flatMap(decoder::fromJson);
        if (result.isFailure()) {
            Option<A> defaultValue = decoder.defaultValue();
            result = defaultValue.map(DecodeResult::ok).getOrElse(result);
            if (result.isFailure()) {
                result = result.fold(
                        err -> DecodeResult.fail(err.contains(String.format("'%s' not found", name)) ? err : String.format("'%s' failed with message: '%s'", name, err)),
                        DecodeResult::ok
                );
            }
        }
        return result;
    }

    public final static class Ok<A> extends DecodeResult<A> {
        public final A value;

        public Ok(A value) {
            this.value = value;
        }

        @Override
        public <B> B fold(Function<String, B> failFunction, Function<A, B> okFunction) {
            return okFunction.apply(this.value);
        }

        @Override
        public void consume(Consumer<String> failFunction, Consumer<A> okFunction) {
            okFunction.accept(this.value);
        }

        @Override
        public <X extends Throwable> A getOrElseThrow(Function<String, X> exProvider) throws X {
            return value;
        }

        public A getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("Ok(value='%s')", value);
        }
    }

    public final static class Failure extends DecodeResult<Object> {
        public final String message;

        public Failure(String message) {
            this.message = message;
        }

        @Override
        public <B> B fold(Function<String, B> failFunction, Function<Object, B> okFunction) {
            return failFunction.apply(this.message);
        }

        @Override
        public <X extends Throwable> Object getOrElseThrow(Function<String, X> exProvider) throws X {
            throw exProvider.apply(message);
        }

        @Override
        public void consume(Consumer<String> failFunction, Consumer<Object> okFunction) {
            failFunction.accept(this.message);
        }

        @Override
        public String toString() {
            return String.format("Failure(message='%s')", message);
        }
    }

}
