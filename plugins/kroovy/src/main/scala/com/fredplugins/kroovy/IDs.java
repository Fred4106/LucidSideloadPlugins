package com.fredplugins.kroovy;

import lombok.extern.slf4j.Slf4j;
import scala.Int;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class IDs
{

	private final Set<Integer> ids;

	public IDs(IntStream idsStream)
	{
		this.ids = idsStream.boxed().collect(Collectors.toUnmodifiableSet());
	}
	private IDs(int[] ids)
	{
		this(Arrays.stream(ids));
	}

	public static IDs empty = IDs.of();
//	public static IDs fromStream(IntStream stream) {
//		return new IDs(stream);
//	}

	public static IDs of(Object... args) {
		int[] temp =  Arrays.stream(args).flatMapToInt(c -> {
			if(c instanceof IDs) {
				return ((IDs) c).ids.stream().mapToInt(Integer::intValue);
			} else if(c instanceof Int) {
				return IntStream.of(((Int) c).toInt());
			} else if(c instanceof Integer) {
				return IntStream.of((Integer) c);
			} else {
				log.warn("Cant find handler for value {} with class {}", c, c.getClass());
				return IntStream.empty();
			}
		}).toArray();
		return new IDs(temp);
	}
//	public IDs(int[] ids)
//	{
//		Collections.unmodifiableSet(Set.of(
//		this.ids =  Collections.unmodifiableSet(Set.of(ids));
//	}
//	// map various types to a single set of integers
//	// handles int/int[]/IDs
//	public IDs(Object... of)
//	{
//		this(Stream.of(of).map(IDs::objectToInts).flatMapToInt(IntStream::of).boxed().collect(Collectors.toSet()));
//	}

	@SuppressWarnings("ChainOfInstanceofChecks")
//	private static int[] objectToInts(Object o)
//	{
//		if (o instanceof int[]) {
//			return (int[]) o;
//		}
//		if (o instanceof Integer) {
//			return new int[]{(int) o};
//		}
//		if (o instanceof Int) {
//			return new int[]{((Int) o).toInt()};
//		}
//		if (o instanceof IDs) {
//			return ((IDs) o).build();
//		}
//		System.out.println("unsupported type + " + o.getClass());
//		throw new IllegalArgumentException(String.format("Unsupported type: %s", o.getClass()));
//	}

	public boolean contains(int id)
	{
		return this.ids.contains(id);
	}

	public int[] build()
	{
		return this.ids.stream().mapToInt(Integer::intValue).toArray();
	}

	@Override
	public String toString()
	{
		return "IDs{" + this.ids + '}';
	}

}