package test;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiffValues extends Object {
    private String expectedValue;

    private String capturedValue;
}
