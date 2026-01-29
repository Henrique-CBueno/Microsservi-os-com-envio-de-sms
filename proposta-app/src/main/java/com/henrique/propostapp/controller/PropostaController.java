package com.henrique.propostapp.controller;

import com.henrique.propostapp.dto.PropostaCreateReqDTO;
import com.henrique.propostapp.dto.PropostaCreateResDTO;
import com.henrique.propostapp.sevice.PropostaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("proposta")
@RequiredArgsConstructor
public class PropostaController {

    private final PropostaService propostaService;

    @PostMapping
    public ResponseEntity<PropostaCreateResDTO> criar(@RequestBody PropostaCreateReqDTO dto) {
        PropostaCreateResDTO response = propostaService.criarProposta(dto);
        return ResponseEntity.created(getLocation(response.id()))
                .body(response);
    }


    @GetMapping
    public ResponseEntity<List<PropostaCreateResDTO>> getAll() {
        List<PropostaCreateResDTO> todasPropostas = propostaService.getAll();
        return ResponseEntity.ok(todasPropostas);
    }







    private static URI getLocation(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
