package com.example.frontendweb.controller;

import com.example.frontendweb.dto.ClienteDTO;
import com.example.frontendweb.dto.VendaDTO;
import com.example.frontendweb.dto.VendedorDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    private final RestClient restClient;

    public VendaController(RestClient restClient) {
        this.restClient = restClient;
    }

    @Value("${api.base-url}")
    private String apiBaseUrl;

    @GetMapping
    public String listar(Model model) {
        List<VendaDTO> vendas = restClient.get()
                .uri("/vendas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<VendaDTO>>() {});

        model.addAttribute("apiBaseUrl", apiBaseUrl);
        model.addAttribute("vendas", vendas);
        return "vendas/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("venda", new VendaDTO());
        model.addAttribute("clientes", buscarClientes());
        model.addAttribute("vendedores", buscarVendedores());
        return "vendas/form";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        VendaDTO venda = restClient.get()
                .uri("/vendas/{id}", id)
                .retrieve()
                .body(VendaDTO.class);

        model.addAttribute("venda", venda);
        model.addAttribute("clientes", buscarClientes());
        model.addAttribute("vendedores", buscarVendedores());
        return "vendas/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute VendaDTO vendaDTO,
                         @RequestParam(value = "cliente.id", required = false) Long clienteId) throws IOException {

        ClienteDTO clienteComId = (clienteId != null) ? new ClienteDTO(clienteId, null, null) : null;

        VendaDTO payload = new VendaDTO(
                vendaDTO.id(),
                vendaDTO.data(),
                vendaDTO.valor(),
                vendaDTO.cliente(),
                vendaDTO.vendedor()
        );

        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        HttpHeaders jsonHeader = new HttpHeaders();
        jsonHeader.setContentType(MediaType.APPLICATION_JSON);
        multipartBody.add("venda", new org.springframework.http.HttpEntity<>(payload, jsonHeader));

        if (payload.id() == null) {
            restClient.post()
                    .uri("/vendas")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipartBody)
                    .retrieve()
                    .toBodilessEntity();
        } else {
            restClient.put()
                    .uri("/vendas/{id}", payload.id())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(multipartBody)
                    .retrieve()
                    .toBodilessEntity();
        }
        return "redirect:/vendas";
    }

    private List<ClienteDTO> buscarClientes() {
        return restClient.get()
                .uri("/clientes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ClienteDTO>>() {});
    }

    private List<VendedorDTO> buscarVendedores() {
        return restClient.get()
                .uri("/vendedores")
                .retrieve()
                .body(new ParameterizedTypeReference<List<VendedorDTO>>() {});
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        restClient.delete()
                .uri("/vendas/{id}", id)
                .retrieve()
                .toBodilessEntity();

        return "redirect:/vendas";
    }
}