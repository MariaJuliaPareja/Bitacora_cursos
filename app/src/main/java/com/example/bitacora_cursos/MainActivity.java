package com.example.bitacora_cursos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText txtTema;
    private Spinner spinAreas, spinSecciones;
    private Button btnRegistrar;
    private DatabaseReference clasesRef;
    private ListView listaClases;
    private Button btnActualizar, btnEliminar;
    private ArrayList<Clases> clasesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Clases claseSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clasesRef = FirebaseDatabase.getInstance().getReference("Clases");

        txtTema = findViewById(R.id.txttema);
        spinAreas = findViewById(R.id.spinarea);
        spinSecciones = findViewById(R.id.spinseccion);
        btnRegistrar = findViewById(R.id.btnregistrar);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarClase();
            }
        });
        btnActualizar = findViewById(R.id.btnactualizar);
        btnEliminar = findViewById(R.id.btneliminar);
        listaClases = findViewById(R.id.listaclases);

        leerClases();

        listaClases.setOnItemClickListener((parent, view, position, id) -> {
            claseSeleccionada = clasesList.get(position);
            txtTema.setText(claseSeleccionada.getTema());
        });

        btnActualizar.setOnClickListener(v -> {
            if (claseSeleccionada != null) {
                String temaNuevo = txtTema.getText().toString();
                claseSeleccionada.setTema(temaNuevo);
                clasesRef.child("Lecciones").child(claseSeleccionada.getClaseid()).setValue(claseSeleccionada);
                Toast.makeText(this, "Clase actualizada", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            }
        });
        btnEliminar.setOnClickListener(v -> {
            if (claseSeleccionada != null) {
                clasesRef.child("Lecciones").child(claseSeleccionada.getClaseid()).removeValue();
                Toast.makeText(this, "Clase eliminada", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            }
        });
    }

    public void registrarClase() {
        // Obtener los datos de la UI
        String claseid = txtTema.getText().toString().trim();  // Usar un ID proporcionado
        String seccion = spinSecciones.getSelectedItem().toString();
        String area = spinAreas.getSelectedItem().toString();
        String tema = txtTema.getText().toString().trim();

        if (!TextUtils.isEmpty(tema)) {
            Clases leccion = new Clases(claseid, seccion, area, tema);
            clasesRef.child("Lecciones").child(claseid).setValue(leccion);
            Toast.makeText(this, "Clase registrada", Toast.LENGTH_LONG).show();
        } else  {
            Toast.makeText(this, "Debe introducir un tema", Toast.LENGTH_LONG).show();
        }
    }
    private void leerClases() {
        clasesRef.child("Lecciones").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                clasesList.clear();
                ArrayList<String> datos = new ArrayList<>();

                for (DataSnapshot dato : snapshot.getChildren()) {
                    Clases clase = dato.getValue(Clases.class);
                    clasesList.add(clase);
                    datos.add(clase.getTema() + " - " + clase.getSeccion() + " - " + clase.getArea());
                }

                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, datos);
                listaClases.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error al leer clases", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void limpiarCampos() {
        txtTema.setText("");
        claseSeleccionada = null;
    }
}
