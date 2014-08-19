package generador_grafo;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.RejectedExecutionException;

import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.text.DefaultCaret;

import busquedaGrafo.RecorreGrafo;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class Principal {

	private JFrame frame;
	private GenerarClientes clientes;
	private boolean debug=true;
	private boolean autoinserta=false;
	private JTextField txtMinsecciones;
	private JTextField txtMaxsecciones;
	private JTextField txtMaxcruces;
	private JTextField txtMincruces;
	private GenerarGrafo grafo;
	private JTextField txtMinclientes;
	private JTextField txtMaxclientes;
	private JTextField txtMinconsumo;
	private JTextField txtMaxconsumo;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Principal window = new Principal();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Principal() {
		initialize();
	}
	
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener (){
		public void windowClosing(WindowEvent e) {
		apagaCassandra();
		System.exit(0);
		}
		@Override
		public void windowActivated(WindowEvent e) {}
		@Override
		public void windowClosed(WindowEvent e) {}
		@Override
		public void windowDeactivated(WindowEvent e) {}
		@Override
		public void windowDeiconified(WindowEvent e) {}
		@Override
		public void windowIconified(WindowEvent e) {}
		@Override
		public void windowOpened(WindowEvent e) {}
		});
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnArchivo = new JMenu("Archivo");
		menuBar.add(mnArchivo);
		
		JMenuItem mntmArrancaCassandra = new JMenuItem("Arranca cassandra");
		mnArchivo.add(mntmArrancaCassandra);
		
		mntmArrancaCassandra.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enciendeProcesoCassandra();
			}
		});
		
		JMenuItem mntmPintagrafo = new JMenuItem("Pinta grafo");
		mntmPintagrafo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				grafo.setDebug(debug);
				grafo.pintaTabla();
				if(autoinserta&&esperaConexionCassandra()){
				grafo.pintaBD();
				}
			}
		});
		mnArchivo.add(mntmPintagrafo);
		
		JMenuItem mntmPintaClientes = new JMenuItem("Pinta clientes");
		mntmPintaClientes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientes.setDebug(debug);
				clientes.pintaTabla();
				if(autoinserta&&esperaConexionCassandra()){
				clientes.pintaBD();
				}
			}
		});
		mnArchivo.add(mntmPintaClientes);
		
		JMenuItem mntmRecreaBd = new JMenuItem("Recrea BD");
		mnArchivo.add(mntmRecreaBd);
		mntmRecreaBd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				limpiarBD();
			}
		});
		
		
		JMenu mnOpciones = new JMenu("Opciones");
		menuBar.add(mnOpciones);
		
		JCheckBoxMenuItem chckbxmntmDebug = new JCheckBoxMenuItem("Debug");
		chckbxmntmDebug.setSelected(true);
		mnOpciones.add(chckbxmntmDebug);
		chckbxmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		        
				Principal.this.setDebug(((AbstractButton) e.getSource()).getModel().isSelected());
			}
		});
		
		JCheckBoxMenuItem chckbxmntmAutoInserta = new JCheckBoxMenuItem("Auto inserta");
		chckbxmntmAutoInserta.setSelected(false);
		mnOpciones.add(chckbxmntmAutoInserta);
		chckbxmntmAutoInserta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		        
				Principal.this.setAutoInserta(((AbstractButton) e.getSource()).getModel().isSelected());
			}
		});
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.WEST);
		
		Box verticalBox = Box.createVerticalBox();
		panel.add(verticalBox);
		
		JLabel lblGenerarSecciones = new JLabel("Generar Secciones");
		verticalBox.add(lblGenerarSecciones);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		JLabel lblMinsecciones = new JLabel("MinSecciones:");
		horizontalBox.add(lblMinsecciones);
		
		txtMinsecciones = new JTextField();
		horizontalBox.add(txtMinsecciones);
		txtMinsecciones.setText("10");
		txtMinsecciones.setColumns(10);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		JLabel lblMaxsecciones = new JLabel("MaxSecciones:");
		horizontalBox_1.add(lblMaxsecciones);
		
		txtMaxsecciones = new JTextField();
		horizontalBox_1.add(txtMaxsecciones);
		txtMaxsecciones.setText("15");
		txtMaxsecciones.setColumns(10);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_2);
		
		JLabel lblMinCruces = new JLabel("MinCruces:");
		horizontalBox_2.add(lblMinCruces);
		
		txtMincruces = new JTextField();
		txtMincruces.setText("3");
		txtMincruces.setColumns(10);
		horizontalBox_2.add(txtMincruces);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_3);
		
		JLabel lblMaxCruces = new JLabel("MaxCruces:");
		horizontalBox_3.add(lblMaxCruces);
		
		txtMaxcruces = new JTextField();
		horizontalBox_3.add(txtMaxcruces);
		txtMaxcruces.setText("4");
		txtMaxcruces.setColumns(10);
		
		Box verticalBox_1 = Box.createVerticalBox();
		panel.add(verticalBox_1);
		
		JLabel lblGenerarClientes = new JLabel("Generar Clientes");
		verticalBox_1.add(lblGenerarClientes);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_4);
		
		JLabel lblMinclientes = new JLabel("MinClientes:");
		horizontalBox_4.add(lblMinclientes);
		
		txtMinclientes = new JTextField();
		txtMinclientes.setText("3");
		txtMinclientes.setColumns(10);
		horizontalBox_4.add(txtMinclientes);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_5);
		
		JLabel lblMaxclientes = new JLabel("MaxClientes:");
		horizontalBox_5.add(lblMaxclientes);
		
		txtMaxclientes = new JTextField();
		txtMaxclientes.setText("4");
		txtMaxclientes.setColumns(10);
		horizontalBox_5.add(txtMaxclientes);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_6);
		
		JLabel lblMinconsumo = new JLabel("MinConsumo:");
		horizontalBox_6.add(lblMinconsumo);
		
		txtMinconsumo = new JTextField();
		txtMinconsumo.setText("1100");
		txtMinconsumo.setColumns(10);
		horizontalBox_6.add(txtMinconsumo);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_7);
		
		JLabel lblMaxconsumo = new JLabel("MaxConsumo:");
		horizontalBox_7.add(lblMaxconsumo);
		
		txtMaxconsumo = new JTextField();
		txtMaxconsumo.setText("3500");
		txtMaxconsumo.setColumns(10);
		horizontalBox_7.add(txtMaxconsumo);
		
		Box verticalBox_2 = Box.createVerticalBox();
		panel.add(verticalBox_2);
		
		JButton btnGenerarsecciones = new JButton("Generar Secciones");
		verticalBox_2.add(btnGenerarsecciones);
		btnGenerarsecciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 Thread generarSeccionesThread = new Thread() {
				      public void run() {
				 grafo = new GenerarGrafo(Principal.this);
				 grafo.setDebug(debug);
				 grafo.generar(Integer.parseInt(txtMinsecciones.getText()),Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2);
				 if(debug)grafo.pintaTabla();
				 if(autoinserta&&esperaConexionCassandra()){
				 grafo.insertaGrafoCQL(true);
				 }
				      }
				    };
				    generarSeccionesThread.start();
			}
		});
		
		JButton btnGenerarClientes = new JButton("Generar Clientes");
		verticalBox_2.add(btnGenerarClientes);
		btnGenerarClientes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		  
				 Thread generarClientesThread = new Thread() {
				      public void run() {
				 clientes = new GenerarClientes(grafo,Principal.this);
				clientes.setDebug(debug);
				clientes.generar(Integer.parseInt(txtMinclientes.getText()),Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));
				 if(debug)clientes.pintaTabla();
				if(autoinserta){
					if(esperaConexionCassandra()){
				clientes.insertaGrafoCQL(true);
				}
			     }
					    }};
					    generarClientesThread.start();
		}
		});
		
		JButton btnHaztodo = new JButton("Haz todo");
		verticalBox_2.add(btnHaztodo);
		btnHaztodo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread haztodoThread = new Thread() {
				      public void run() {
				    	  if(esperaConexionCassandra()){
								 limpiarBD();
								 grafo = new GenerarGrafo(Principal.this);
								 grafo.setDebug(debug);
								 grafo.generar(Integer.parseInt(txtMinsecciones.getText()),Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2);
								 grafo.pintaTabla();
								 grafo.insertaGrafoCQL(true);
								 grafo.pintaBD();
								 clientes = new GenerarClientes(grafo,Principal.this);
								 clientes.setDebug(debug);
								 clientes.generar(Integer.parseInt(txtMinclientes.getText()),Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));
								 clientes.conectarClientes(grafo.numeroSecciones());
								 clientes.insertaGrafoCQL(true); 
								 clientes.pintaBD();
							}
				      }
				    };
				    haztodoThread.start();
			}
		});
		
		JButton btnEncuentraCaminos = new JButton("Encuentra Caminos");
		verticalBox_2.add(btnEncuentraCaminos);
		btnEncuentraCaminos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread encuentraCaminosThread = new Thread() {
				      public void run() {
				    	  if(esperaConexionCassandra()){
				    		  limpiarBD();
								 grafo = new GenerarGrafo(Principal.this);
								 grafo.setDebug(debug);
								 grafo.generar(Integer.parseInt(txtMinsecciones.getText()),Integer.parseInt(txtMaxsecciones.getText()),Integer.parseInt(txtMincruces.getText()),Integer.parseInt(txtMaxcruces.getText()),1,2);
								 grafo.pintaTabla();
								 grafo.insertaGrafoCQL(true);
								 grafo.pintaBD();
								 clientes = new GenerarClientes(grafo,Principal.this);
								 clientes.setDebug(debug);
								 clientes.generar(Integer.parseInt(txtMinclientes.getText()),Integer.parseInt(txtMaxclientes.getText()),Integer.parseInt(txtMinconsumo.getText()),Integer.parseInt(txtMaxconsumo.getText()));
								 clientes.conectarClientes(grafo.numeroSecciones());
								 clientes.insertaGrafoCQL(true); 
								 clientes.pintaBD();
				    			RecorreGrafo recorre = new RecorreGrafo(Principal.this);
								recorre.setDebug(true);
								System.out.println("1\n");
								System.out.println("Numero vertices:"+recorre.consultaNumeroVertices());
								recorre.consultaExtremoGrafoSQL();
								//recorre.consultaExtremoGrafoSQL(0);
								recorre.caminosGenerados();
							}
				      }
				    };
				    encuentraCaminosThread.start();
		
			}
		});
		
		final JTextArea textArea = new JTextArea();
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JScrollPane scroll = new JScrollPane (textArea);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	          scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

	    frame.getContentPane().add(scroll, BorderLayout.SOUTH);

		textArea.setRows(22);
		//frame.getContentPane().add(textArea, BorderLayout.SOUTH);
		JTextAreaOutputStream out = new JTextAreaOutputStream (textArea);
		System.setOut (new PrintStream (out));
        
	}
	
	private void limpiarBD(){
		if(enciendeProcesoCassandra() && esperaConexionCassandra()){
	      if(debug)System.out.printf("Conectado al cluster: %s\n", this.getCassandraCluster().getMetadata().getClusterName());
	      if(debug){
	    	  for ( Host host : this.getCassandraCluster().getMetadata().getAllHosts() ) {
	         System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
	               host.getDatacenter(), host.getAddress(), host.getRack());
	      }
	      }
	      Session session = cluster.connect();
		   try{
		     session.execute("USE Bd"); 
		     session.execute("DROP KEYSPACE Bd;");
		   } catch (InvalidQueryException e){ 
		   }
		}
	}
	
	public void setDebug(boolean debug){
		this.debug=debug;		
	}
	
	public void setAutoInserta(boolean insertar){
		this.autoinserta=insertar;
	}
	
	public Cluster getCassandraCluster(){
		return cluster;		
	}
	
	public Session getCassandraSession(){
		return session;
	}
	
	public class JTextAreaOutputStream extends OutputStream
	{
	    private final JTextArea destination;

	    public JTextAreaOutputStream (JTextArea destination)
	    {
	        if (destination == null)
	            throw new IllegalArgumentException ("Destination is null");

	        this.destination = destination;
	    }

	    @Override
	    public void write(byte[] buffer, int offset, int length) throws IOException
	    {
	        final String text = new String (buffer, offset, length);
	        SwingUtilities.invokeLater(new Runnable ()
	            {
	                @Override
	                public void run() 
	                {
	                    destination.append (text);
	                }
	            });
	    }

	    @Override
	    public void write(int b) throws IOException
	    {
	        write (new byte [] {(byte)b}, 0, 1);
	    }
	}
	
	 private void apagaCassandra() {
		 /*
		    try {
		      session.close();
		      String line="cmd /c tasklist.exe /v  | find "+"\""+"Cassandra"+"\"";
		      Process p = Runtime.getRuntime().exec(line);
		      BufferedReader input = new BufferedReader
		          (new InputStreamReader(p.getInputStream()));
		      while ((line = input.readLine()) != null) {
		          if (!line.trim().equals("")) {
		              // keep only the process name
		        	  System.out.println(line.split("\\s{1,}")[1]);
		              p = Runtime.getRuntime().exec("TASKKILL /PID "+line.split("\\s{1,}")[1]);
		          }

		      }
		      input.close();
		    }
		    catch (Exception err) {
		      err.printStackTrace();
		    }*/
		  }
	 
	private boolean enciendeProcesoCassandra(){
		boolean encendido = false;
		encendido=compruebaProcesoCassandra();
		if(!encendido){
		File currDir = new File(".\\apache-cassandra-2.0.9\\bin\\cassandra.bat");
	    String path = currDir.getAbsolutePath();
	    path = path.substring(0, path.length());
	    String dir = path.substring(0, path.length()-13);
		try {
			Runtime.getRuntime().exec("cmd /c cd \""+dir+"\" && start cassandra.bat");
			Thread.sleep(2000);
			encendido=compruebaProcesoCassandra();
		} catch (IOException e) {
			encendido=false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			encendido=false;
		}
		}
		System.out.println("Resultado enciendeProcesoCassandra:"+encendido);
		return encendido;
	}
	 
	private boolean compruebaProcesoCassandra(){
		boolean encendido=false;
		 try {
		      String line="cmd /c tasklist.exe /v  | find "+"\""+"CassandraMaster"+"\"";
		      Process p = Runtime.getRuntime().exec(line);
		      BufferedReader input = new BufferedReader
		          (new InputStreamReader(p.getInputStream()));
		      while ((line = input.readLine()) != null && !encendido) {
		          if (!line.trim().equals("")) {
		            encendido=true;
		          }

		      }
		      input.close();
		    }
		    catch (Exception err) {
		      encendido = false;
		    }
		System.out.println("Resultado compruebaProcesoCassandra:"+encendido);
		return encendido;		
	}
	
	private Cluster cluster = null;
	private Session session = null;
	
	private boolean esperaConexionCassandra() {
		    boolean encendido=false;
			int tiempo = 0;
			int espera=3;
			int timeout=15;
			encendido=false;
			if(enciendeProcesoCassandra()){
			while(!encendido && tiempo < (timeout/espera)){
				try{
					cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
					Metadata metadata = cluster.getMetadata();
				      if(debug)System.out.printf("Conectado al cluster: %s\n", metadata.getClusterName());
				      if(debug){
				    	  for ( Host host : metadata.getAllHosts() ) {
				         System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
				               host.getDatacenter(), host.getAddress(), host.getRack());
				      }
				      }
				      session = cluster.connect();
				      
					encendido=true;
				} catch (NoHostAvailableException e){ 
				    try {
				    	System.out.println("Intentando reconectar en "+espera+"s");
						Thread.sleep(espera*1000);
						timeout++;
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				   }
				catch (RejectedExecutionException e2){ 
				    try {
				    	System.out.println("Intentando reconectar en "+espera+"s");
						Thread.sleep(espera*1000);
						timeout++;
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				   }
				}
			}
			if(tiempo==timeout/espera){System.out.println("Dio timeout");}
		    return encendido;
	}
	}
