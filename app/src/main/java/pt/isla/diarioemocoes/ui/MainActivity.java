package pt.isla.diarioemocoes.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.isla.diarioemocoes.R;

/**
 * ACTIVITY PRINCIPAL: MainActivity
 *
 * Passo 1: AppCompatActivity fornece a ActionBar automaticamente
 * via o tema Theme.DiarioEmocoes — requisito obrigatório do enunciado.
 */
public class MainActivity extends AppCompatActivity {

    private RegistoEmocaoViewModel viewModel;
    private EditText editTextEstado;
    private EditText editTextNotas;
    private RegistoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Passo 2: Inflar o layout definido em res/layout/activity_main.xml
        setContentView(R.layout.activity_main);

        // Passo 3: Ligar variáveis Java aos elementos XML pelo ID
        editTextEstado = findViewById(R.id.editTextEstado);
        editTextNotas  = findViewById(R.id.editTextNotas);
        Button buttonGuardar = findViewById(R.id.buttonGuardar);
        Button buttonLimpar  = findViewById(R.id.buttonLimpar);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewRegistos);

        // Passo 4: Configurar RecyclerView com adapter e layout manager
        adapter = new RegistoAdapter(id -> mostrarDialogoConfirmacaoApagar(id));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Passo 5: Instanciar ViewModel via ViewModelProvider (forma correcta — sem new)
        viewModel = new ViewModelProvider(this).get(RegistoEmocaoViewModel.class);

        // Passo 6: Observar LiveData — a UI actualiza-se automaticamente quando os dados mudam
        viewModel.todosOsRegistos.observe(this, registos -> adapter.submitList(registos));

        // =====================================================================
        // Passo 7: BOTÃO GUARDAR com validação + Toast (requisito do enunciado)
        // =====================================================================
        buttonGuardar.setOnClickListener(v -> {
            String estado = editTextEstado.getText().toString().trim();
            String notas  = editTextNotas.getText().toString().trim();

            if (TextUtils.isEmpty(estado)) {
                Toast.makeText(this, R.string.toast_campos_vazios, Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.guardarRegisto(estado, notas, 0.0);
            Toast.makeText(this, R.string.toast_guardado, Toast.LENGTH_SHORT).show();
            editTextEstado.setText("");
            editTextNotas.setText("");
        });

        // =====================================================================
        // Passo 8: BOTÃO LIMPAR com AlertDialog (requisito do enunciado)
        // Acção destrutiva requer confirmação explícita — boas práticas de UX
        // =====================================================================
        buttonLimpar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_limpar_titulo)
                    .setMessage(R.string.dialog_limpar_mensagem)
                    .setPositiveButton(R.string.dialog_confirmar, (dialog, which) -> {
                        editTextEstado.setText("");
                        editTextNotas.setText("");
                        Toast.makeText(this, R.string.toast_limpo, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.dialog_cancelar, null)
                    .show();
        });
    }

    /**
     * Passo 9: AlertDialog de confirmação antes de apagar um registo.
     * Chamado pelo RegistoAdapter via callback OnApagarClickListener.
     */
    private void mostrarDialogoConfirmacaoApagar(long id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_apagar_titulo)
                .setMessage(R.string.dialog_apagar_mensagem)
                .setPositiveButton(R.string.dialog_confirmar, (dialog, which) -> {
                    viewModel.apagarRegisto(id);
                    Toast.makeText(this, R.string.toast_eliminado, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }
}
