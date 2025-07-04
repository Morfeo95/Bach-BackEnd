package com.bach.api.api.rests;

import com.bach.api.api.types.DTOActualizacionPerfil;
import com.bach.api.api.types.DTORespuestaPerfil;
import com.bach.api.config.security.TokenService;
import com.bach.api.jpa.repositories.PerfilRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/perfiles")
@SecurityRequirement(name = "bearer-key")
public class PerfilController {

    @Autowired
    private PerfilRepository repository;

    @Autowired
    private TokenService tokenService;


    //el perfil se crea con datos independietes
    @PutMapping("/actualizar")
    @Transactional
    public ResponseEntity<DTORespuestaPerfil> actualizaPerfil(@RequestBody DTOActualizacionPerfil datos,
                                                              @RequestHeader("Authorization") String token){
        var usuarioId = tokenService.getClaimId(token);
        var perfilOptional = repository.findByUsuarioId(usuarioId);
        if (perfilOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var perfil = perfilOptional.get();
        perfil.actualizate(datos);
        var datosRespuesta = new DTORespuestaPerfil(perfil);
        return ResponseEntity.ok(datosRespuesta);
    }

    //obtener tu perfil con tu token
    @GetMapping("/obtener-perfil")
    public ResponseEntity<DTORespuestaPerfil> obtienePerfil(@RequestHeader("Authorization") String token){
        var usuarioId = tokenService.getClaimId(token);
        var perfilOptional = repository.findByUsuarioId(usuarioId);
        if (perfilOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var perfil = perfilOptional.get();
        var datosRespuesta = new DTORespuestaPerfil(perfil);
        return ResponseEntity.ok(datosRespuesta);
    }

    //obtener perfil de usuario x con su id de perfil
    @GetMapping("/obtener-perfil/{usuarioId}")
    public ResponseEntity<DTORespuestaPerfil> obtienePerfilPorId (@PathVariable Long usuarioId, @RequestHeader("Authorization") String token){
        var perfilOptional = repository.findByUsuarioId(usuarioId);
        if (perfilOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var perfil = perfilOptional.get();
        var datosRespuesta = new DTORespuestaPerfil(perfil);
        return ResponseEntity.ok(datosRespuesta);
    }
}
