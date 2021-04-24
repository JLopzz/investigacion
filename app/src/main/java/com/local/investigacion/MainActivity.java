package com.local.investigacion;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    Button add, delete;
    ListView lv;
    private DatabaseReference db;
    private ArrayList state;
    private ArrayAdapter adap;
    private String newId,editId;
    EditText name, age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Referencia de Base de datos
        db = FirebaseDatabase.getInstance().getReference("clientes");
        //edit texts
        name = findViewById(R.id.editTextTextPersonName);
        age = findViewById(R.id.editTextNumber);
        //lista de estado que se relaciona al adaptador
        state = new ArrayList();
        //Adaptador de ListView, para los cambios
        adap = new ArrayAdapter(this, android.R.layout.simple_list_item_1,state);
        lv = findViewById(R.id._dynamic);
        lv.setAdapter(adap);
        //Referencia de botones
        add = findViewById(R.id.button);
        delete = findViewById(R.id.button2);
        //Visualizacion inicial de base de datos
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                state.clear();
                ArrayList<Map> value =(ArrayList<Map>) dataSnapshot.getValue();
                newId = String.valueOf(value.size());
                for (Map a: value)
                    if(!Objects.isNull(a)) {
                        String nombre = (String) a.get("nombre");
                        Long edad = (Long) a.get("edad");
                        Long cod = (Long) a.get("codigo");
                        state.add("Codigo: " + cod + ", Nombre: " + nombre + ", Edad: " + edad);
                    }
                adap.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("errorFirebase", "Failed to read value.", error.toException());
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String currentState =(String) state.get(i);
                Pattern pat = Pattern.compile(", Nombre: (.*), Edad: (\\d+)");
                Matcher mat = pat.matcher(currentState);
                mat.find();
                Log.i("debug","Nombre: "+mat.group(1)+" edad: "+mat.group(2));

                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(MainActivity.this);
                dialogo1.setTitle("Opciones de Registros");
                dialogo1.setMessage("¿ Desea Editar o Eliminar el registro?");
                dialogo1.setCancelable(true);
                dialogo1.setPositiveButton("Editar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        add.setEnabled(false);
                        delete.setEnabled(true);
                        name.setText(mat.group(1));
                        age.setText(mat.group(2));
                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                newReg(String.valueOf(i));
                            }
                        });
                        Toast.makeText(getApplicationContext(), "Click en Editar, para Editar registro", LENGTH_SHORT).show();

                    }
                });
                dialogo1.setNegativeButton("Eliminar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dial1, int id) {
                        db.child(String.valueOf(i)).removeValue();
                        Toast.makeText(getApplicationContext(), "Se ha eliminado con Exito", LENGTH_SHORT).show();
                    }
                });
                dialogo1.show();
            }
        });

    }


    public void add(View view) {
        newReg(newId);
    }

    private void newReg(String id){
        if(!name.getText().toString().equals("") || !age.getText().toString().equals("")){
            Map info = new HashMap();
            info.put("nombre", name.getText().toString());
            info.put("edad", Long.parseLong(age.getText().toString()));
            info.put("codigo", Long.parseLong(id));
            db.child(id).setValue(info);
            Toast.makeText(this, "Se ha agregado el registro con exito", LENGTH_SHORT).show();
            name.setText("");
            age.setText("");
            delete.setEnabled(false);
            add.setEnabled(true);
        }
        else Toast.makeText(this, "No se puede dejar campos vacios", LENGTH_SHORT).show();
    }
}