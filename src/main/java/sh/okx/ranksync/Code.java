package sh.okx.ranksync;

import java.util.UUID;
import lombok.Data;

@Data
public class Code {
  private final UUID uniqueId;
  private final long timestamp;
}
