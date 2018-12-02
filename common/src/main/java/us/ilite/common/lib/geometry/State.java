package us.ilite.common.lib.geometry;

import us.ilite.common.lib.util.CSVWritable;
import us.ilite.common.lib.util.Interpolable;

public interface State<S> extends Interpolable<S>, CSVWritable {
    double distance(final S other);

    boolean equals(final Object other);

    String toString();

    String toCSV();
}
