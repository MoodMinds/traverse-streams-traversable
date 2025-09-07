# Implementation of [Traverse Streams](https://github.com/MoodMinds/traverse-streams)

This project offers fundamental implementation and widely-used transformations for [Traverse Streams](https://github.com/MoodMinds/traverse-streams).

## Overview

The **Traverse Streams Traversable** extends the `TraverseSupport` interface, introducing the `Traversable` interface.
It delivers various `Traversable` implementations that encompass standard stream transformations like mapping, filtering,
flattening, and more. These transformations are encapsulated in distinct classes, providing versatility and pre-built
combinations of `Traversable`s to easily construct a robust data stream pipeline.

Moreover, the `Traversable` interface incorporates the `Traversable.Resolver`, enabling the resolution of result values
from the source. This can involve reduction operations using a specified `java.util.stream.Collector` or short-circuiting
operations such as reducing `reduce`, retrieving the first matching item value based on a predicate - `any`, or obtaining
a boolean flag indicating the presence of a value in the source (`anyMatch`, `allMatch`, `noneMatch` methods).

## Traversables

- **BeforeTraversable**: Emits items from a `Traversable` before execution of another `Traversable`.
- **CatchTraversable**: Performs traversal of a `Traversable` allowing exceptions handling/retrying on caught exception.
- **ConcatTraversable**: Performs item concatenation of the specified `Traversable` instances.
- **ContextTraversable**: Performs transformation of an `Association` context before traversal.
- **DeferTraversable**: Defers traversal to a `Traversable` returned by a specified supplier.
- **DropTraversable**: Retains items of a `Traversable` dropping items while they pass a specified predicate.
- **EffectTraversable**: Executes a specified code without emitting items.
- **ExceptTraversable**: Raises an `Exception` using a specified supplier.
- **FilterTraversable**: Applies a specified predicate for filtering items in a `Traversable`.
- **FinaleTraversable**: Emits items from a `Traversable` and finally executes another `Traversable`.
- **FlattenTraversable**: Applies a specified item flattening function, returning a new `Traversable`.
- **FollowTraversable**: Emits items from a `Traversable` after execution of another `Traversable`.
- **IterateTraversable**: Emits items using a specified iterative function.
- **LimitTraversable**: Truncates items of a `Traversable` to a specified number size.
- **MapTraversable**: Applies a specified function for mapping items in a `Traversable`.
- **PeekTraversable**: Accepts a specified consumer for peeking items in a `Traversable`.
- **ResolveTraversable**: Performs resolution of a `Traversable` to a result by a specified `Traversable.Resolver`.
- **SkipTraversable**: Retains items of a `Traversable` skipping a specified number size.
- **SortedTraversable**: Performs a `Traversable` items collecting and sorting before traversal.
- **StreamTraversable**: Emits items from a specified Java Stream supplier.
- **SupplyTraversable**: Emits items using a specified supplier infinitely or n-times.
- **TakeTraversable**: Truncates items of a `Traversable` taking items while they pass a specified predicate.
- **UniqueTraversable**: Preserves uniqueness (distinct) in items in a `Traversable`.

## Code Samples

```java
import org.moodminds.elemental.OptionalNullable;
import org.moodminds.traverse.Traversable;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.moodminds.traverse.BeforeTraversable.before;
import static org.moodminds.traverse.CatchTraversable.resume;
import static org.moodminds.traverse.CatchTraversable.retry;
import static org.moodminds.traverse.ConcatTraversable.concat;
import static org.moodminds.traverse.ContextTraversable.context;
import static org.moodminds.traverse.DeferTraversable.defer;
import static org.moodminds.traverse.DropTraversable.drop;
import static org.moodminds.traverse.EffectTraversable.effect;
import static org.moodminds.traverse.ExceptTraversable.except;
import static org.moodminds.traverse.FilterTraversable.filter;
import static org.moodminds.traverse.FinaleTraversable.finale;
import static org.moodminds.traverse.FlattenTraversable.flatten;
import static org.moodminds.traverse.FollowTraversable.follow;
import static org.moodminds.traverse.IterateTraversable.iterate;
import static org.moodminds.traverse.LimitTraversable.limit;
import static org.moodminds.traverse.MapTraversable.map;
import static org.moodminds.traverse.PeekTraversable.peek;
import static org.moodminds.traverse.ResolveTraversable.resolve;
import static org.moodminds.traverse.SkipTraversable.skip;
import static org.moodminds.traverse.SortedTraversable.sorted;
import static org.moodminds.traverse.StreamTraversable.stream;
import static org.moodminds.traverse.SupplyTraversable.supply;
import static org.moodminds.traverse.TakeTraversable.take;
import static org.moodminds.traverse.Traversable.allMatch;
import static org.moodminds.traverse.Traversable.any;
import static org.moodminds.traverse.Traversable.anyMatch;
import static org.moodminds.traverse.Traversable.each;
import static org.moodminds.traverse.Traversable.noneMatch;
import static org.moodminds.traverse.Traversable.reduce;
import static org.moodminds.traverse.UniqueTraversable.unique;

public class Sample {

    String item() throws IOException {
        return "value"; // imagine the method can throw IOException somehow
    }

    // throws IOException because of throwing Traversable<String, IOException> itself
    void sample1() throws IOException {
        Traversable<String, IOException> traversable = supply(this::item);
        boolean completion0 = traversable.sequence(t -> t.next(out::println)); // returning false, not all consumed
        boolean completion1 = traversable.traverse(t -> t.each(out::println)); // returning true, all consumed
        // or
        boolean completion2 = traversable.traverse(each(out::println));
    }

    // throws IOException because of throwing Traversable<String, IOException> itself
    // and ParseException because of throwing traverse function
    void sample2() throws IOException, ParseException {
        Traversable<String, IOException> traversable = supply(this::item);
        boolean completion = traversable.traverse(t -> t.each(s -> {
            if ("".equals(s))
                throw new ParseException("", 0);
            out.println(s);
        }));
    }

    // traverse resolving with the specified Resolver
    void sample3() throws IOException {
        Traversable<String, IOException> traversable = supply(this::item);
        String join = traversable.sequence(joining(", "));
        List<String> list = traversable.parallel(toList());
        OptionalNullable<String> any = traversable.traverse(any(s -> true));
        boolean anyMatch = traversable.sequence(anyMatch("a"::equals));
        boolean allMatch = traversable.traverse(allMatch("b"::equals));
        boolean noneMatch = traversable.parallel(noneMatch("c"::equals));
    }

    void sample5() {
        Traversable<Void, Exception> tr0 = effect(() -> {});
        Traversable<String, Exception> tr1 = stream(asList("abc", "def", "gh")::stream);
        Traversable<String, Exception> tr2 = map(tr1, String::toUpperCase);
        Traversable<String, Exception> tr3 = peek(tr1, out::println);
        Traversable<String, Exception> tr4 = filter(tr2, s -> s.length() == 3);
        Traversable<String, Exception> tr5 = follow(tr2, tr3);
        Traversable<String, Exception> tr6 = concat(tr1, tr2, tr5);
        Traversable<String, Exception> tr7 = defer(() -> tr6);
        Traversable<String, Exception> tr8 = sorted(tr7);
        Traversable<String, Exception> tr9 = unique(tr8);
        Traversable<String, Exception> tr10 = before(tr2, tr3);
        Traversable<String, Exception> tr11 = finale(tr2, tr3);
        Traversable<String, Exception> tr12 = supply(this::item);
        Traversable<String, Exception> tr13 = supply(this::item, 10);
        Traversable<String, Exception> tr14 = resolve(tr12, reduce(joining(", ")));
        Traversable<Boolean, Exception> tr15 = resolve(tr11, anyMatch("a"::equals));
        Traversable<Integer, Exception> tr16 = iterate(0, i -> i + 1);
        Traversable<Integer, Exception> tr17 = iterate(0, i -> i + 1, i -> i < 10);
        Traversable<Boolean, Exception> tr18 = except(Exception::new);
        Traversable<Character, Exception> tr19 = flatten(tr9, s -> stream(() -> s.chars().mapToObj(i -> (char) i)));
        Traversable<Character, Exception> tr20 = limit(tr19, 15);
        Traversable<Character, Exception> tr21 = skip(tr20, 3);
        Traversable<Character, Exception> tr22 = take(tr21, c -> c != 'G');
        Traversable<Character, Exception> tr23 = drop(tr22, c -> c != 'D');
        Traversable<Character, Exception> tr24 = context(tr23, (ctx, write) -> write.put("v", 23).remove("t"));
        Traversable<Character, Exception> tr25 = retry(tr24, 2);
        Traversable<Character, Exception> tr26 = resume(tr25, t ->
                stream(() -> t.getMessage().chars().mapToObj(i -> (char) i)));
    }
}
```

## Maven configuration

Artifacts can be found on [Maven Central](https://search.maven.org/) after publication.

```xml
<dependency>
    <groupId>org.moodminds.traverse</groupId>
    <artifactId>traverse-streams-traversable</artifactId>
    <version>${version}</version>
</dependency>
```

## Building from Source

You may need to build from source to use **Traverse Streams Traversable** (until it is in Maven Central) with Maven and JDK 9 at least.

## License
This project is going to be released under version 2.0 of the [Apache License][l].

[l]: https://www.apache.org/licenses/LICENSE-2.0