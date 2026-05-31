package pt.isla.diarioemocoes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import pt.isla.diarioemocoes.R;
import pt.isla.diarioemocoes.data.RegistoEmocao;

/**
 * ADAPTER: RegistoAdapter
 *
 * Passo 1: ListAdapter é a evolução moderna do RecyclerView.Adapter.
 * A diferença fundamental reside no DiffUtil: em vez de notifyDataSetChanged()
 * (que redesenha TODA a lista, custoso em performance), o DiffUtil calcula
 * a diferença mínima entre a lista antiga e a nova, animando apenas
 * os itens que realmente mudaram — otimização crítica em listas longas.
 */
public class RegistoAdapter extends ListAdapter<RegistoEmocao, RegistoAdapter.RegistoViewHolder> {

    /**
     * Passo 2: Interface funcional para o callback de eliminação.
     * O adapter não deve conhecer o ViewModel diretamente — apenas comunica
     * ao exterior (MainActivity) que o utilizador quer apagar um item,
     * passando o ID. A Activity decide o que fazer com essa informação.
     */
    public interface OnApagarClickListener {
        void onApagarClick(long id);
    }

    private final OnApagarClickListener listener;

    /**
     * Passo 3: DiffUtil.ItemCallback define como o ListAdapter compara itens.
     * 'areItemsTheSame': compara identidade (mesma linha na BD?) — usa o ID.
     * 'areContentsTheSame': compara conteúdo (os dados mudaram?) — usa o estado e notas.
     * Se apenas o conteúdo mudar, o item é animado em vez de recriado.
     */
    private static final DiffUtil.ItemCallback<RegistoEmocao> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RegistoEmocao>() {
                @Override
                public boolean areItemsTheSame(@NonNull RegistoEmocao oldItem,
                                               @NonNull RegistoEmocao newItem) {
                    // IDs iguais = mesmo registo na base de dados
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull RegistoEmocao oldItem,
                                                  @NonNull RegistoEmocao newItem) {
                    // Conteúdo igual = não é necessário redesenhar o item
                    return oldItem.getEstadoEmocional().equals(newItem.getEstadoEmocional())
                            && oldItem.getNotasTexto().equals(newItem.getNotasTexto());
                }
            };

    public RegistoAdapter(OnApagarClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * Passo 4: onCreateViewHolder — cria a vista física para um item da lista.
     * Chamado apenas quando não há ViewHolder reciclável disponível.
     * O LayoutInflater converte o XML do item em objeto View em memória.
     */
    @NonNull
    @Override
    public RegistoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registo, parent, false);
        return new RegistoViewHolder(itemView);
    }

    /**
     * Passo 5: onBindViewHolder — preenche uma vista reciclada com dados novos.
     * Chamado sempre que um item entra no ecrã. Aqui apenas lemos dados — nunca
     * fazemos operações pesadas como queries à BD ou parsing de imagens.
     */
    @Override
    public void onBindViewHolder(@NonNull RegistoViewHolder holder, int position) {
        RegistoEmocao registo = getItem(position);
        holder.textViewEstado.setText(registo.getEstadoEmocional());
        holder.textViewData.setText(registo.getDataHoraLegivel());
        holder.textViewNotas.setText(registo.getNotasTexto());
        // Passa o ID do registo para o callback de eliminação
        holder.buttonApagar.setOnClickListener(v -> listener.onApagarClick(registo.getId()));
    }

    /**
     * Passo 6: ViewHolder — padrão de design que guarda referências aos elementos
     * de UI de cada item da lista. Evita chamadas repetidas a findViewById()
     * (operação lenta) em cada scroll — essencial para 60fps estável.
     */
    static class RegistoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewEstado;
        TextView textViewData;
        TextView textViewNotas;
        Button buttonApagar;

        RegistoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewEstado = itemView.findViewById(R.id.textViewEstado);
            textViewData = itemView.findViewById(R.id.textViewData);
            textViewNotas = itemView.findViewById(R.id.textViewNotas);
            buttonApagar = itemView.findViewById(R.id.buttonApagarItem);
        }
    }
}
