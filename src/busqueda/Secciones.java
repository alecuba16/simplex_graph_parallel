package busqueda;

import java.util.ArrayList;
import java.util.List;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.InvalidQueryException;

import comun.Gestor;

public class Secciones {

  public ArrayList secciones= new ArrayList();
  
  public int[] agregaSeccion(int idSeccion,float coste, int maximo, int vertA, int vertB){
	  int pos[]=new int[2];
	  pos[0]=-1;
	  pos[1]=0;
	  pos=buscaPosSiExiste(idSeccion,vertA, vertB);
	  if(pos[0]==-1){
		  Seccion nuevaSeccion = new Seccion();
		  nuevaSeccion.idSeccion=idSeccion;
		  nuevaSeccion.coste=coste;
		  nuevaSeccion.maximo=maximo;
		  nuevaSeccion.vertA=vertA;
		  nuevaSeccion.vertB=vertB;
		  secciones.add(nuevaSeccion);
		  pos[0]=secciones.indexOf(nuevaSeccion);
		  pos[1]=0;
	  }
	  return pos;
  } 
  
  private int[] buscaPosSiExiste(int idSeccion, int vertA, int vertB){
	  //System.out.println("Entra busca pos idSeccion:"+idSeccion+" vertA"+vertA+" vertB"+vertB);
	  int posEncontrado[]=new int[2];
	  posEncontrado[0]=-1;
	  posEncontrado[1]=0;//0 normal al guardado //1 inverso
	  int i=-1;;
	  int micopiai=i;
	  if(secciones.size()>0){
		  while(posEncontrado[0]==-1&&i<secciones.size()){
			  /*//omp parallel if(secciones.size()>1) shared(posEncontrado,i) private(micopiai)*/
			  //{
				/*//omp critical buscaPosSiExiste*/
				//{
				 i++;
				 micopiai=i;				 
				//}
				if(micopiai<secciones.size()){
					 //System.out.println("1 "+jomp.runtime.OMP.getThreadNum());
					 Seccion seccion=((Seccion)secciones.get(micopiai));
					if(seccion!=null&&seccion.idSeccion==idSeccion){
						if(seccion.vertA == vertA && seccion.vertB == vertB) {
						/*//omp critical buscaPosSiExiste*/
						//{
							 posEncontrado[0]=micopiai;
							 posEncontrado[1]=0;
						//}
						}else{
							/*//omp critical buscaPosSiExiste*/
							//{
							posEncontrado[0]=micopiai;
							 posEncontrado[1]=1;
							//}
						}
					}
				}
				 //System.out.println("3 "+jomp.runtime.OMP.getThreadNum()+" "+jomp.runtime.OMP.getNumThreads());
				 
				 //System.out.println("4 "+jomp.runtime.OMP.getThreadNum());
			  //}
		  } 
	  }
	  //System.out.println("Sale busca pos");
	  return posEncontrado;
  }
  
  public int getVertBSeccionID(int id){
	  int vertB=-1;
	  if(id>=0&&id<secciones.size()&& ((Seccion)secciones.get(id))!=null){
		  vertB=((Seccion)secciones.get(id)).vertB;
	  }
	  return vertB;
  }
  
  public int getVertASeccionID(int id){
	  int vertA=-1;
	  if(id>=0&&id<secciones.size()&& ((Seccion)secciones.get(id))!=null){
		  	vertA=((Seccion)secciones.get(id)).vertA;
	  }
	  return vertA;
  }
  
  public int getidSeccion(int id){
	  Seccion seccion = ((Seccion)secciones.get(id));
	  return seccion.idSeccion;
  }
 
  public boolean rellenaCaracteristicas(Gestor gestor){
	  boolean error=false;
	  for(int i=0;i<secciones.size();i++){
		  Statement  consulta1 = new SimpleStatement("SELECT idseccion,consumoMax,coste FROM Bd.caracteristicasVertices WHERE idseccion="+((Seccion)secciones.get(i)).idSeccion);
		  ResultSet  resultsA=null;
		  try {
				resultsA = gestor.getCassandraSession().execute(consulta1);
				error=false;
			  } catch (InvalidQueryException e) {
					System.out.println("Error al obtener datos de la secci\u00f3n "+i);
				e.printStackTrace();
				error=true;
		  }
		  if(!error){
			if(resultsA!=null){
				List rowsA = (List)resultsA.all();
				if(rowsA.size()>0){
					for(int j=0;j<rowsA.size();j++){
						((Seccion)secciones.get(i)).coste = ((Row) rowsA.get(j)).getFloat("coste");
						((Seccion)secciones.get(i)).maximo = ((Row) rowsA.get(j)).getInt("consumoMax");
					}
				}
			}
		  }
	  }
	  return error;	
   }
  
  public int getLimiteSeccion(int id){
	  Seccion seccion = ((Seccion)secciones.get(id));
	  return seccion.maximo;
  }
  
  public float getCosteSeccion(int id){
	  Seccion seccion = ((Seccion)secciones.get(id));
	  return seccion.coste;
  }
  
  public void limpia(){
	 secciones= new ArrayList();
  }}



