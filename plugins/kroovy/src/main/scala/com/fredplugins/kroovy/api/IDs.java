package com.fredplugins.kroovy.api;

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