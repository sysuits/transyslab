package com.transyslab.commons.io;

import java.time.LocalDateTime;
import java.util.List;

public interface Spatiotemporal {
    List<Object> select(LocalDateTime fromTime, LocalDateTime toTime, String simRegion, String sourceName);
}
