CREATE TABLE evaluaciones(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    evauador_id BIGINT NOT NULL,
    desafio_id BIGINT,
    mentoria_id BIGINT,
    video_id BIGINT NOT NULL,
    puntuacion DOUBLE NOT NULL,
    comentario TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (desafio_id) REFERENCES desafios(id),
    FOREIGN KEY (mentoria_id) REFERENCES mentorias(id),
    FOREIGN KEY (video_id) REFERENCES video(id)
);