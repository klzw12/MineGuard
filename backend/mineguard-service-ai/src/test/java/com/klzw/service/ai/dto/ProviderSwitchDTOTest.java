package com.klzw.service.ai.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProviderSwitchDTOTest {

    @Test
    void testSetterGetter() {
        ProviderSwitchDTO dto = new ProviderSwitchDTO();
        dto.setProvider("minimax");

        assertEquals("minimax", dto.getProvider());
    }

    @Test
    void testNoArgsConstructor() {
        ProviderSwitchDTO dto = new ProviderSwitchDTO();
        assertNull(dto.getProvider());
    }

    @Test
    void testProviderSwitch() {
        ProviderSwitchDTO dto = new ProviderSwitchDTO();
        dto.setProvider("deepseek");
        assertEquals("deepseek", dto.getProvider());

        dto.setProvider("minimax");
        assertEquals("minimax", dto.getProvider());
    }
}
