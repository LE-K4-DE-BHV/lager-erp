package com.lagermanagement.space.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lagermanagement.space.domain.enums.ArtikelStatus;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.service.ArtikelService;
import com.lagermanagement.space.web.dto.ArtikelCreateRequest;
import com.lagermanagement.space.web.dto.ArtikelDto;
import com.lagermanagement.space.web.dto.ArtikelUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtikelController.class)
@WithMockUser(username = "operator", roles = "OPERATOR")
class ArtikelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArtikelService artikelService;

    private static final ArtikelDto ARTIKEL_DTO = new ArtikelDto(
            1L, "ART-001", "Testartikel", "Stück", "Elektronik",
            null, null, 10, 5, 8, 20, new BigDecimal("9.99"), ArtikelStatus.AKTIV);

    @Test
    void shouldReturnArtikelListOnGetAll() throws Exception {
        when(artikelService.findAll()).thenReturn(List.of(ARTIKEL_DTO));

        mockMvc.perform(get("/api/v1/artikel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].artikelnummer").value("ART-001"));
    }

    @Test
    void shouldReturnArtikelOnGetById() throws Exception {
        when(artikelService.findById(1L)).thenReturn(ARTIKEL_DTO);

        mockMvc.perform(get("/api/v1/artikel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bezeichnung").value("Testartikel"));
    }

    @Test
    void shouldReturn404WhenArtikelNotFound() throws Exception {
        when(artikelService.findById(99L)).thenThrow(new EntityNotFoundException("Artikel mit ID 99 nicht gefunden."));

        mockMvc.perform(get("/api/v1/artikel/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn201OnArtikelCreate() throws Exception {
        ArtikelCreateRequest request = new ArtikelCreateRequest(
                "ART-001", "Testartikel", "Stück", "Elektronik",
                null, 5, 8, 20, new BigDecimal("9.99"));
        when(artikelService.create(any())).thenReturn(ARTIKEL_DTO);

        mockMvc.perform(post("/api/v1/artikel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.artikelnummer").value("ART-001"));
    }

    @Test
    void shouldReturn400WhenArtikelCreateMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/artikel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200OnArtikelUpdate() throws Exception {
        ArtikelUpdateRequest request = new ArtikelUpdateRequest(
                "Geänderter Artikel", "Stück", "Elektronik",
                null, 5, 8, 20, new BigDecimal("9.99"));
        when(artikelService.update(eq(1L), any())).thenReturn(ARTIKEL_DTO);

        mockMvc.perform(put("/api/v1/artikel/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenArtikelDeaktivieren() throws Exception {
        ArtikelDto inaktiv = new ArtikelDto(
                1L, "ART-001", "Testartikel", "Stück", "Elektronik",
                null, null, 10, 5, 8, 20, new BigDecimal("9.99"), ArtikelStatus.INAKTIV);
        when(artikelService.deaktivieren(1L)).thenReturn(inaktiv);

        mockMvc.perform(put("/api/v1/artikel/1/deaktivieren").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INAKTIV"));
    }

    @Test
    void shouldReturnBestandOnGetBestand() throws Exception {
        when(artikelService.getBestand(1L)).thenReturn(10);

        mockMvc.perform(get("/api/v1/artikel/1/bestand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aktuellerBestand").value(10));
    }
}
