package net.hamnaberg.json.io;

import io.vavr.control.Option;
import io.vavr.control.Try;
import net.hamnaberg.json.Json;
import net.hamnaberg.json.codec.DecodeJson;
import net.hamnaberg.json.codec.DecodeResult;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class JsonParser {

    public final Try<Json.JValue> parse(ReadableByteChannel channel) {
        return parse(Channels.newInputStream(channel));
    }

    public final Try<Json.JValue> parse(InputStream is) {
        return parse(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
    }

    public final Try<Json.JValue> parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes));
    }

    public final Try<Json.JValue> parse(String string) {
        return parse(new StringReader(string));
    }

    public final Try<Json.JValue> parse(Reader reader) {
        BufferedReader buf;
        if (reader instanceof BufferedReader) {
            buf = (BufferedReader) reader;
        } else {
            buf = new BufferedReader(reader);
        }
        return Try.of(() -> {
            try (Reader r = buf) {
                return parseImpl(r);
            }
        }).flatMap(Function.identity());
    }

    public final Json.JValue parseUnsafe(ReadableByteChannel channel) {
        return parseUnsafe(Channels.newInputStream(channel));
    }

    public final Json.JValue parseUnsafe(InputStream is) {
        return parseUnsafe(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
    }

    public final Json.JValue parseUnsafe(byte[] bytes) {
        return parseUnsafe(new ByteArrayInputStream(bytes));
    }

    public final Json.JValue parseUnsafe(String string) {
        return parseUnsafe(new StringReader(string));
    }

    public final Json.JValue parseUnsafe(Reader reader) {
        return parse(reader).getOrElseThrow(e -> {
            if (!(e instanceof JsonParseException)) {
                throw new JsonParseException(e);
            }
            throw ((JsonParseException)e);
        });
    }

    public final Option<Json.JValue> parseOpt(ReadableByteChannel is) {
        return parse(is).toOption();
    }

    public final Option<Json.JValue> parseOpt(byte[] bytes) {
        return parse(bytes).toOption();
    }

    public final Option<Json.JValue> parseOpt(InputStream is) {
        return parse(is).toOption();
    }

    public final Option<Json.JValue> parseOpt(String string) {
        return parse(string).toOption();
    }

    public final Option<Json.JValue> parseOpt(Reader reader) {
        return parse(reader).toOption();
    }

    public final <A> DecodeResult<A> decode(ReadableByteChannel is, DecodeJson<A> decoder) {
        return decode(parse(is), decoder);
    }

    public final <A> DecodeResult<A> decode(byte[] bytes, DecodeJson<A> decoder) {
        return decode(parse(bytes), decoder);
    }

    public final <A> DecodeResult<A> decode(InputStream is, DecodeJson<A> decoder) {
        return decode(parse(is), decoder);
    }

    public final <A> DecodeResult<A> decode(String string, DecodeJson<A> decoder) {
        return decode(parse(string), decoder);
    }

    public final <A> DecodeResult<A> decode(Reader reader, DecodeJson<A> decoder) {
        return decode(parse(reader), decoder);
    }

    public final <A> DecodeResult<A> decode(Try<Json.JValue> parsed, DecodeJson<A> decoder) {
        return parsed.map(decoder::fromJson).getOrElseGet(t -> DecodeResult.fail(t.getMessage()));
    }

    protected abstract Try<Json.JValue> parseImpl(Reader reader);
}
