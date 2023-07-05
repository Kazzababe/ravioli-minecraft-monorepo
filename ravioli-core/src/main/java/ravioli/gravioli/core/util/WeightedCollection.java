package ravioli.gravioli.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public final class WeightedCollection<T> {
	private final NavigableMap<Double, T> entries = new TreeMap<>();
	private final Random random;

	private double total;
	
	public WeightedCollection() {
		this(new Random());
	}
	
	public WeightedCollection(@NotNull final Random random) {
		this.random = random;
	}
	
	public void add(final double weight, @NotNull final T object) {
        if (weight <= 0) {
            return;
        }
		this.total += weight;
		this.entries.put(this.total, object);
	}

	public int size() {
		return this.entries.size();
	}
	
	public @Nullable T next() {
		final double value = this.random.nextDouble(total) + 1;

		return this.entries.ceilingEntry(value).getValue();
	}
	
}
