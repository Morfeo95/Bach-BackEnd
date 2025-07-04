package com.bach.api.api.rests;

import com.bach.api.api.types.DTOActualizacionDesafio;
import com.bach.api.api.types.DTORegistroDesafio;
import com.bach.api.api.types.DTORespuestaDesafio;
import com.bach.api.config.security.TokenService;
import com.bach.api.jpa.entities.Desafio;
import com.bach.api.jpa.entities.Notification;
import com.bach.api.jpa.entities.Usuario;
import com.bach.api.jpa.entities.UsuarioMentoria;
import com.bach.api.jpa.enums.Role;
import com.bach.api.jpa.repositories.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/desafios")
@SecurityRequirement(name = "bearer-key")
public class DesafioController {

    @Autowired
    private DesafioRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MentoriaRepository mentoriaRepository;

    @Autowired
    private UsuarioMentoriaRepository usuarioMentoriaRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    //los desafios se crean con las mentorias
    @PostMapping("/crear-desafio/{mentoriaId}")
    public ResponseEntity<DTORespuestaDesafio> crearDesafio(@PathVariable Long mentoriaId, @RequestBody DTORegistroDesafio datos,
                                                            @RequestHeader ("Authorization") String token){
        Role rolDeUsuario = Role.valueOf(tokenService.getClaimrol(token));
        if (rolDeUsuario != Role.ADMIN && rolDeUsuario != Role.MENTOR){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var mentoriaOptional = mentoriaRepository.findById(mentoriaId);
        if (mentoriaOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var mentoria = mentoriaOptional.get();
        var curoso = mentoria.getCurso();
        var desafio = new Desafio(datos, mentoria, curoso);
        repository.save(desafio);
        var datosRespuesta = new DTORespuestaDesafio(desafio);
        for (UsuarioMentoria um : usuarioMentoriaRepository.findAll()) {
            if (um.getUsuario().isActivo() && um.isCompletada()) {
                Notification n = new Notification(um.getUsuario(), "DESAFIO",
                        "Nuevo Desafio: " + desafio.getTitulo());
                notificationRepository.save(n);
            }
        }
        return ResponseEntity.ok(datosRespuesta);
    }

    //se pueden actualizar solo los datos del DTO
    @PutMapping("/actualizar-desafio/{idDesafio}")
    @Transactional
    public ResponseEntity<DTORespuestaDesafio> actualizaDesafio(@PathVariable Long idDesafio,
                                                                @RequestBody DTOActualizacionDesafio datos, @RequestHeader("Authorization") String token){
        Role rolDeUsuario = Role.valueOf(tokenService.getClaimrol(token));
        if (rolDeUsuario != Role.ADMIN && rolDeUsuario != Role.MENTOR){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var desafioOptional = repository.findById(idDesafio);
        if(desafioOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var desafio = desafioOptional.get();
        desafio.actualiza(datos);
        var datosRespuesta = new DTORespuestaDesafio(desafio);
        return ResponseEntity.ok(datosRespuesta);
    }

    //listar todos los desafios
    @GetMapping("/todos-los-desafios")
    public ResponseEntity<Page<DTORespuestaDesafio>> listarDesafios(Pageable pageable){
        var desafios = repository.findAll(pageable).map(DTORespuestaDesafio::new);
        if (desafios.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(desafios);
    }

    //listar desafios por mentoria
    @GetMapping("/desafios-por-mentoria/{idMentoria}")
    public ResponseEntity<Page<DTORespuestaDesafio>> listarDesafiosPorMentoria(@PathVariable Long idMentoria, Pageable pageable){
        var desafios = repository.findByMentoriaId(idMentoria,pageable).map(DTORespuestaDesafio::new);
        if (desafios.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(desafios);
    }

    //lista los desafios de las mentorias que ya terminaste
    @GetMapping("/desafios-por-mentoria-completada/{idMentoria}")
    public ResponseEntity<Page<DTORespuestaDesafio>> listarDesafiosPorMentoriaCompletada(@PathVariable Long idMentoria, Pageable pageable,
                                                                                         @RequestHeader("Authorization")String token){
        var desafios = repository.findByMentoriaId(idMentoria,pageable).map(DTORespuestaDesafio::new);
        var usuarioId = tokenService.getClaimId(token);
        if (desafios.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var completado = usuarioMentoriaRepository.existsByMentoriaIdAndUsuarioIdAndCompletadaTrue(idMentoria,usuarioId);
        if (!completado){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(desafios);
    }

    //lista los desafios del curso
    @GetMapping("/desafios-por-curso/{idCurso}")
    public ResponseEntity<Page<DTORespuestaDesafio>> listarDesafiosPorcurso(@PathVariable Long idCurso, Pageable pageable){
        var desafios = repository.findByCursoId(idCurso,pageable).map(DTORespuestaDesafio::new);
        if (desafios.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(desafios);
    }

    //borra totalmete los desafios de la db
    @DeleteMapping("/{idDesafio}")
    public ResponseEntity borraDesafio(@PathVariable Long idDesafio, @RequestHeader("Authorization") String token){
        Role rolDeUsuario = Role.valueOf(tokenService.getClaimrol(token));
        if (rolDeUsuario != Role.ADMIN && rolDeUsuario != Role.MENTOR){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var desafioOptional = repository.findById(idDesafio);
        if (desafioOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var desafio = desafioOptional.get();
        repository.delete(desafio);
        return  ResponseEntity.ok("Desafio Eliminado");
    }
}
