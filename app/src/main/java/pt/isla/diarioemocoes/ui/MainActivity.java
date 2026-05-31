package pt.isla.diarioemocoes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.isla.diarioemocoes.R;
import pt.isla.diarioemocoes.rgpd.PrivacidadeActivity;
import pt.isla.diarioemocoes.security.ValidadorDados;

/**
 * ACTIVITY PRINCIPAL: MainActivity
 *
 * Boas práticas implementadas:
 * - Action Bar com menu (Privacidade + Sobre) — requisito do enunciado
 * - Validação de input via ValidadorDados antes de passar ao ViewModel
 * - Contagem de registos observada para feedback ao utilizador
 * - Mensagens de erro específicas da validação em vez de genéricas
 * - Acessibilidade: contentDescription em todos os elementos interactivos
 */
public class MainActivity extends AppCompatActivity {

    private RegistoEmocaoViewModel viewModel;
    private EditText editTextEstado;
    private EditText editTextNotas;
    private RegistoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEstado = findViewById(R.id.editTextEstado);
        editTextNotas  = findViewById(R.id.editTextNotas);
        Button buttonGuardar = findViewById(R.id.buttonGuardar);
        Button buttonLimpar  = findViewById(R.id.buttonLimpar);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewRegistos);

        adapter = new RegistoAdapter(id -> mostrarDialogoConfirmacaoApagar(id));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(RegistoEmocaoViewModel.class);
        viewModel.todosOsRegistos.observe(this, registos -> adapter.submitList(registos));

        // Observar total de registos para título dinâmico da ActionBar
        viewModel.totalRegistos.observe(this, total -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(
                        total != null && total > 0
                                ? total + " registo(s) armazenado(s)"
                                : "Sem registos"
                );
            }
        });

        // =====================================================================
        // BOTÃO GUARDAR — validação via ValidadorDados antes de persistir
        // =====================================================================
        buttonGuardar.setOnClickListener(v -> {
            String estado = editTextEstado.getText().toString();
            String notas  = editTextNotas.getText().toString();

            // Validar estado
            ValidadorDados.ResultadoValidacao vEstado = ValidadorDados.validarEstado(estado);
            if (!vEstado.valido) {
                Toast.makeText(this, vEstado.mensagemErro, Toast.LENGTH_SHORT).show();
                editTextEstado.requestFocus();
                return;
            }

            // Validar notas
            ValidadorDados.ResultadoValidacao vNotas = ValidadorDados.validarNotas(notas);
            if (!vNotas.valido) {
                Toast.makeText(this, vNotas.mensagemErro, Toast.LENGTH_SHORT).show();
                editTextNotas.requestFocus();
                return;
            }

            boolean guardou = viewModel.guardarRegisto(estado, notas, 0.0);
            if (guardou) {
                Toast.makeText(this, R.string.toast_guardado, Toast.LENGTH_SHORT).show();
                editTextEstado.setText("");
                editTextNotas.setText("");
            }
        });

        // =====================================================================
        // BOTÃO LIMPAR com AlertDialog
        // =====================================================================
        buttonLimpar.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_limpar_titulo)
                    .setMessage(R.string.dialog_limpar_mensagem)
                    .setPositiveButton(R.string.dialog_confirmar, (d, w) -> {
                        editTextEstado.setText("");
                        editTextNotas.setText("");
                        Toast.makeText(this, R.string.toast_limpo, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.dialog_cancelar, null)
                    .show()
        );
    }

    // =========================================================================
    // ACTION BAR — Menu com Privacidade e Sobre (requisito do enunciado)
    // =========================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_privacidade) {
            startActivity(new Intent(this, PrivacidadeActivity.class));
            return true;
        }
        if (id == R.id.action_sobre) {
            mostrarDialogoSobre();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoSobre() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sobre_titulo)
                .setMessage(R.string.sobre_mensagem)
                .setPositiveButton(R.string.dialog_cancelar, null)
                .show();
    }

    private void mostrarDialogoConfirmacaoApagar(long id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_apagar_titulo)
                .setMessage(R.string.dialog_apagar_mensagem)
                .setPositiveButton(R.string.dialog_confirmar, (d, w) -> {
                    viewModel.apagarRegisto(id);
                    Toast.makeText(this, R.string.toast_eliminado, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }
}
