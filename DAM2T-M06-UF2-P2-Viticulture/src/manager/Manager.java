package manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.Bodega;
import model.Campo;
import model.Entrada;
import model.Vid;
import utils.TipoVid;

public class Manager {
	private static Manager manager;
	MongoCollection<Document> collection;
	MongoDatabase database;
	private ArrayList<Entrada> entradas;
	private Session session;
	private Transaction tx;
	private Bodega b;
	private Campo c;

	private Manager () {
		this.entradas = new ArrayList<>();
	}
	
	public static Manager getInstance() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}
	
	/*private void createSession() {
		org.hibernate.SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    	session = sessionFactory.openSession();
	}*/
	private void createSession() {
		String uri = "mongodb://localhost:27017";
        MongoClientURI mongoClientURI = new MongoClientURI(uri);
        MongoClient mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase("Viticulture");
	}

	public void init() {
		createSession();
		getEntrada();
		manageActions();
		//showAllCampos();
		//showCantidadVidByTipo();
		//session.close();
	}

	private void manageActions() {
		for (Entrada entrada : this.entradas) {
			try {
				System.out.println(entrada.getInstruccion());
				switch (entrada.getInstruccion().toUpperCase().split(" ")[0]) {
					case "B":
						addBodega(entrada.getInstruccion().split(" "));
						break;
					case "C":
						addCampo(entrada.getInstruccion().split(" "));
						break;
					case "V":
						addVid(entrada.getInstruccion().split(" "));
						break;
					/*case "#":
						vendimia();
						break;*/
					default:
						System.out.println("Instruccion incorrecta");
				}
			} catch (HibernateException e) {
				e.printStackTrace();
				if (tx != null) {
					tx.rollback();
				}
			}
		}
	}

	/*private void vendimia() {
		this.b.getVids().addAll(this.c.getVids());
		
		tx = session.beginTransaction();
		session.save(b);
		
		tx.commit();
	}*/

	/*private void addVid(String[] split) {
		Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]));
		tx = session.beginTransaction();
		session.save(v);
		
		c.addVid(v);
		session.save(c);
		
		tx.commit();
		
	}*/
	private void addVid(String[] split) {
		Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]));
		collection = database.getCollection("Campo");
		Document last = collection.find().sort(new Document("_id",-1)).first();
		collection = database.getCollection("Vid");
		Document document = new Document().append("type",v.getVid().toString()).append("quantity", v.getCantidad()).append("campo", last);
		collection.insertOne(document);
		Document document2 = new Document().append("type", v.getVid().toString()).append("quantity", v.getCantidad());
		collection = database.getCollection("Campo");
		Document update = new Document("$push",new Document("Vid",document2));
		collection.updateOne(document, update);
	}

	/*private void addCampo(String[] split) {
		String nombre = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
		c = new Campo(b, nombre);
		tx = session.beginTransaction();
		int id = (Integer) session.save(c);
		c = session.get(Campo.class, id);
		
		tx.commit();
	}*/
	public void addCampo(String[] split) {
		String nombre = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
		c = new Campo(b, nombre);
		collection = database.getCollection("Bodega");
		Document last = collection.find().sort(new Document("_id",-1)).first();
		collection = database.getCollection("Campo");
		Document document = new Document().append("nombre", c.getNombre()).append("bodega",last);
		collection.insertOne(document);
	}

	/*private void addBodega(String[] split) {
		b = new Bodega(split[1]);
		tx = session.beginTransaction();
		
		int id = (Integer) session.save(b);
		b = session.get(Bodega.class, id);
		
		tx.commit();
		
	}*/
	public void addBodega(String[] split) {
		b = new Bodega(split[1]);
		collection = database.getCollection("Bodega");
		Document document = new Document().append("nombre", b.getNombre());
		collection.insertOne(document);
	}

	/*private void getEntrada() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select e from Entrada e");
		this.entradas.addAll(q.list());
		tx.commit();
	}*/
	private void getEntrada() {
		collection = database.getCollection("Entrada");
		for (Document document : collection.find()) {
			Entrada input = new Entrada();
			input.setInstruccion(document.getString("instruccion"));
			entradas.add(input);
			System.out.println(input);
		}
	}

	/*private void showAllCampos() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select c from Campo c");
		List<Campo> list = q.list();
		for (Campo c : list) {
			System.out.println(c);
		}
		tx.commit();
	}*/

	/*private void showCantidadVidByTipo() {
		tx = session.beginTransaction();
		String hql = "select v.vid, sum(v.cantidad)" + "from Vid v group by v.vid";
	    Query<Object[]> query = session.createQuery(hql, Object[].class);
	    List<Object[]> results = query.getResultList();
	    int sumTipoVidBlanca = 0;
	    int sumTipoVidNegra = 0;
	    for (Object[] result : results) {
	    	TipoVid tipoVid = (TipoVid) result[0];
	        int totalCantidad = ((Number) result[1]).intValue();
	        if (tipoVid == TipoVid.BLANCA) {
	            sumTipoVidBlanca += totalCantidad;
	        } else if (tipoVid == TipoVid.NEGRA) {
	            sumTipoVidNegra += totalCantidad;
	        }
	    }
	    System.out.println("Sum of cantidad where tipo_vid is BLANCA: " + sumTipoVidBlanca);
	    System.out.println("Sum of cantidad where tipo_vid is NEGRA: " + sumTipoVidNegra);
	}*/
}
