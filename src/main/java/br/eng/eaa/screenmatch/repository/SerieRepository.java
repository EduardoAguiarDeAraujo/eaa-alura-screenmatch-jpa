package br.eng.eaa.screenmatch.repository;

import br.eng.eaa.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieRepository extends JpaRepository<Serie, Long> {
}
