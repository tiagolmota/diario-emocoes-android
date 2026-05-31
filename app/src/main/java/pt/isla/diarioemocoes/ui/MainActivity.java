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
 * Passo 1: AppCompatActivity é a base de qualquer Activity moderna no Android.
 * Fornece compatibilidade retroativa com versões antigas do sistema e integra
 * automaticamente a ActionBar (Toolbar) configurada no tema da aplicação.
 * O requisito "Action Bar" do enunciado está satisfeito por esta herança.
 */
public class MainActivity extends AppCompatActivity {

    // Passo 2: Declaração dos componentes de UI como variáveis de instância.
    // Serão inicializados em onCreate após o layout ser inflado.
    private RegistoEmocaoViewModel viewModel;
    private EditText editTextEstado;
    private EditText editTextNotas;
    private RecyclerView recyclerView;
    private RegistoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Passo 3: Inflação do layout — o XML é convertido em objetos Java em memória.
        setContentView(R.layout.activity_main);

        // Passo 4: Ligação das variáveis Java aos elementos definidos no XML (por ID).
        editTextEstado = findViewById(R.id.editTextEstado);
        editTextNotas = findViewById(R.id.editTextNotas);
        Button buttonGuardar = findViewById(R.id.buttonGuardar);
        Button buttonLimpar = findViewById(R.id.buttonLimpar);
        recyclerView = findViewById(R.id.recyclerViewRegistos);

        // Passo 5: Configuração do RecyclerView.
        // LinearLayoutManager empilha os registos verticalmente.
        // O adapter traduz os dados do Room em vistas visuais — será implementado abaixo.
        adapter = new RegistoAdapter(id -> mostrarDialogoConfirmacaoApagar(id));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Passo 6: Instanciação do ViewModel via ViewModelProvider.
        // Esta é a forma CORRETA — nunca usar 'new RegistoEmocaoViewModel()' diretamente,
        // pois perdemos a sobrevivência ao ciclo de vida da Activity.
        viewModel = new ViewModelProvider(this).get(RegistoEmocaoViewModel.class);

        // Passo 7: Observação do LiveData.
        // O 'this' passado como LifecycleOwner garante que o observer é
        // automaticamente removido quando a Activity é destruída — sem memory leaks.
        viewModel.todosOsRegistos.observe(this, registos -> {
            adapter.submitList(registos);
        });

        // =====================================================================
        // Passo 8: BOTÃO GUARDAR — com validação e Toast (requisito do enunciado)
        // =====================================================================
        buttonGuardar.setOnClickListener(v -> {
            String estado = editTextEstado.getText().toString().trim();
            String notas = editTextNotas.getText().toString().trim();

            // Validação: campos obrigatórios não podem estar vazios
            if (TextUtils.isEmpty(estado)) {
                // Toast: feedback rápido e não-intrusivo ao utilizador
                // Toast.LENGTH_SHORT: desaparece após ~2 segundos
                Toast.makeText(this, "Por favor, indique o seu estado emocional.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Temperatura fixa em 0.0 por agora — será lida do sensor em fase posterior
            viewModel.guardarRegisto(estado, notas, 0.0);

            // Toast de confirmação — requisito obrigatório do enunciado
            Toast.makeText(this, "Registo guardado com sucesso!", Toast.LENGTH_SHORT).show();

            // Limpar campos após guardar
            editTextEstado.setText("");
            editTextNotas.setText("");
        });

        // =====================================================================
        // Passo 9: BOTÃO LIMPAR — com AlertDialog (requisito do enunciado)
        // O AlertDialog é usado aqui como confirmação de ação destrutiva,
        // seguindo as Material Design Guidelines para ações irreversíveis.
        // =====================================================================
        buttonLimpar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Limpar campos")
                    .setMessage("Tem a certeza que deseja limpar os campos de texto?")
                    // Botão positivo: executa a ação
                    .setPositiveButton("Limpar", (dialog, which) -> {
                        editTextEstado.setText("");
                        editTextNotas.setText("");
                        Toast.makeText(this, "Campos limpos.", Toast.LENGTH_SHORT).show();
                    })
                    // Botão negativo: cancela sem fazer nada
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    /**
     * Passo 10: AlertDialog de confirmação de eliminação de registo.
     * Chamado pelo adapter quando o utilizador toca no botão "Apagar" de um item.
     * Separar esta lógica num método próprio mantém o onCreate legível — boa prática.
     */
    private void mostrarDialogoConfirmacaoApagar(long id) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Registo")
                .setMessage("Esta ação é irreversível. Confirma que deseja apagar este registo do diário?")
                .setPositiveButton("Apagar", (dialog, which) -> {
                    viewModel.apagarRegisto(id);
                    Toast.makeText(this, "Registo eliminado.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
