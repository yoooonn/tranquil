package com.ycourlee.tranquil.autoconfiguration.crypto.annotation;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yongjiang
 * @date 2021.12.16
 */
@Setter
@Getter
public class CiphertextTest {

    @Ciphertext
    private String hello;
}
