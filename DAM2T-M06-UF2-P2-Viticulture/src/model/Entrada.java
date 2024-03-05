package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name= "Entrada")
public class Entrada {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = true)
	private int id;
	@Column(name = "instruccion")
	private String instruccion;
	@OneToMany //This especifies that for every instance os this class we hav emany of the other
    @JoinColumn(name = "Entrada_id")
	public int getId() {
		return id;
	}
	public String getInstruccion() {
		return instruccion;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setInstruccion(String instruccion) {
		this.instruccion = instruccion;
	}
}
