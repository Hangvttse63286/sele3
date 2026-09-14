package com.sele3.drivers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Platform implements IPlatform {
    CHROME,
    FIREFOX,
    EDGE,
    ;
}
