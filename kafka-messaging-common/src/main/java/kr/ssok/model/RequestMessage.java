package kr.ssok.model;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class RequestMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String correlationId;
    private String data;

    // getters, setters, toString
}
