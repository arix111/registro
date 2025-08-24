// src/main/java/com/registro/service/UsuarioService.java
package com.registro.service;

import com.registro.model.Site;
import com.registro.model.Usuario;
import com.registro.repository.IUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final IUsuarioRepository usuarioRepository;

    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea un usuario nuevo con todos sus datos.
     * Lanza RuntimeException si ya existe uno con ese legajo.
     */
    @Transactional
    public Usuario crearUsuario(String legajo,
                                String nombre,
                                String apellido,
                                String telefono,
                                String correoElectronico,
                                String direccion,
                                Site site) {
        usuarioRepository.findByLegajo(legajo).ifPresent(u ->
            { throw new RuntimeException("Ya existe un usuario con legajo " + legajo); }
        );

        Usuario nuevo = new Usuario();
        nuevo.setLegajo(legajo);
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        nuevo.setTelefono(telefono);
        nuevo.setCorreoElectronico(correoElectronico);
        nuevo.setDireccion(direccion);
        nuevo.setSite(site);

        return usuarioRepository.save(nuevo);
    }

    /**
     * Busca un usuario por su legajo.
     * Lanza RuntimeException si no lo encuentra.
     */
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorLegajo(String legajo) {
        return usuarioRepository.findByLegajo(legajo)
                .orElseThrow(() ->
                    new RuntimeException("Usuario con legajo " + legajo + " no encontrado")
                );
    }

    /**
     * Devuelve todos los usuarios.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Elimina un usuario identificado por su legajo.
     * Lanza RuntimeException si no existe.
     */
    @Transactional
    public void eliminarUsuarioPorLegajo(String legajo) {
        Usuario usuario = usuarioRepository.findByLegajo(legajo)
            .orElseThrow(() ->
                new RuntimeException("No existe usuario con legajo " + legajo)
            );
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public Usuario actualizarUsuario(Usuario usuario) {
    return usuarioRepository.save(usuario);
    }
}
