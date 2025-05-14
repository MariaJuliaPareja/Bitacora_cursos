package com.example.bitacora_cursos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private EditText txtTema;
    private Spinner spinAreas, spinSecciones;
    private Button btnRegistrar, btnActualizar, btnEliminar;
    private DatabaseReference clasesRef;
    private RecyclerView listaClases;
    private ClasesAdapter adapter;
    private ArrayList<Clases> clasesList = new ArrayList<>();
    private Clases claseSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de Firebase
        clasesRef = FirebaseDatabase.getInstance().getReference("Clases");

        // Vistas
        txtTema = findViewById(R.id.txttema);
        spinAreas = findViewById(R.id.spinarea);
        spinSecciones = findViewById(R.id.spinseccion);
        btnRegistrar = findViewById(R.id.btnregistrar);
        btnActualizar = findViewById(R.id.btnactualizar);
        btnEliminar = findViewById(R.id.btneliminar);
        listaClases = findViewById(R.id.listaclases);

        // Configuración de RecyclerView
        listaClases.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClasesAdapter(clasesList, this);
        listaClases.setAdapter(adapter);

        // Listener de registro
        btnRegistrar.setOnClickListener(view -> registrarClase());

        // Listener de actualización
        btnActualizar.setOnClickListener(v -> {
            if (claseSeleccionada != null) {
                String temaNuevo = txtTema.getText().toString();
                claseSeleccionada.setTema(temaNuevo);
                clasesRef.child("Lecciones").child(claseSeleccionada.getClaseid()).setValue(claseSeleccionada);
                Toast.makeText(this, "Clase actualizada", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            }
        });

        // Listener de eliminación
        btnEliminar.setOnClickListener(v -> {
            if (claseSeleccionada != null) {
                clasesRef.child("Lecciones").child(claseSeleccionada.getClaseid()).removeValue();
                Toast.makeText(this, "Clase eliminada", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            }
        });

        // Leer clases desde Firebase
        leerClases();
    }

    // Método para registrar una clase
    public void registrarClase() {
        String claseid = txtTema.getText().toString().trim();
        String seccion = spinSecciones.getSelectedItem().toString();
        String area = spinAreas.getSelectedItem().toString();
        String tema = txtTema.getText().toString().trim();

        if (!TextUtils.isEmpty(tema)) {
            Clases leccion = new Clases(claseid, seccion, area, tema);
            clasesRef.child("Lecciones").child(claseid).setValue(leccion);
            Toast.makeText(this, "Clase registrada", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Debe introducir un tema", Toast.LENGTH_LONG).show();
        }
    }

    // Método para leer las clases desde Firebase de manera optimizada
    private void leerClases() {
        clasesRef.child("Lecciones").limitToFirst(50).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Clases clase = snapshot.getValue(Clases.class);
                clasesList.add(clase);
                adapter.notifyItemInserted(clasesList.size() - 1); // Notificar al adaptador de la nueva clase
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                Clases clase = snapshot.getValue(Clases.class);
                int index = getIndexById(clase.getClaseid());
                if (index != -1) {
                    clasesList.set(index, clase);
                    adapter.notifyItemChanged(index); // Notificar al adaptador de la clase actualizada
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Clases clase = snapshot.getValue(Clases.class);
                int index = getIndexById(clase.getClaseid());
                if (index != -1) {
                    clasesList.remove(index);
                    adapter.notifyItemRemoved(index); // Notificar al adaptador de la clase eliminada
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                // No implementado
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error al leer clases", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Obtener el índice de la clase por su ID
    private int getIndexById(String claseId) {
        for (int i = 0; i < clasesList.size(); i++) {
            if (clasesList.get(i).getClaseid().equals(claseId)) {
                return i;
            }
        }
        return -1;
    }

    // Limpiar los campos del formulario
    private void limpiarCampos() {
        txtTema.setText("");
        claseSeleccionada = null;
    }

    // Definir el adaptador del RecyclerView
    private class ClasesAdapter extends RecyclerView.Adapter<ClasesAdapter.ClasesViewHolder> {
        private final ArrayList<Clases> clases;
        private final MainActivity context;

        public ClasesAdapter(ArrayList<Clases> clases, MainActivity context) {
            this.clases = clases;
            this.context = context;
        }

        @Override
        public ClasesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ClasesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ClasesViewHolder holder, int position) {
            Clases clase = clases.get(position);
            holder.textView.setText(clase.getTema() + " - " + clase.getSeccion() + " - " + clase.getArea());

            holder.itemView.setOnClickListener(v -> {
                claseSeleccionada = clase;
                txtTema.setText(clase.getTema());
            });
        }

        @Override
        public int getItemCount() {
            return clases.size();
        }

        public class ClasesViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ClasesViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
