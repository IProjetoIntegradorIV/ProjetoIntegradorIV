package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.campusconnect.data.model.Establishment
import com.bumptech.glide.Glide

// Adapter para exibir a lista de estabelecimentos em um RecyclerView
class EstablishmentAdapter(private val establishments: List<Establishment>) :
    RecyclerView.Adapter<EstablishmentAdapter.EstablishmentViewHolder>() {

    // ViewHolder para gerenciar os elementos de layout de cada item na lista
    class EstablishmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView) // TextView para o nome do estabelecimento
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView) // TextView para a descrição
        val openingHoursTextView: TextView = view.findViewById(R.id.openingHoursTextView) // TextView para o horário de funcionamento
        val photoImageView: ImageView = view.findViewById(R.id.photoImageView) // ImageView para a foto do estabelecimento
    }

    // Método chamado para criar uma nova ViewHolder para um item da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstablishmentViewHolder {
        // Infla o layout para um item de estabelecimento e cria o ViewHolder com ele
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_establishment, parent, false)
        return EstablishmentViewHolder(view)
    }

    // Método chamado para atualizar o conteúdo de um item da lista
    override fun onBindViewHolder(holder: EstablishmentViewHolder, position: Int) {
        // Obtém o estabelecimento atual na posição especificada
        val establishment = establishments[position]

        // Define os dados do estabelecimento nos elementos de UI do ViewHolder
        holder.nameTextView.text = establishment.name
        holder.descriptionTextView.text = establishment.description
        holder.openingHoursTextView.text = establishment.openingHours

        // Usa Glide para carregar a imagem da URL e aplicar um corte circular
        Glide.with(holder.itemView.context)
            .load(establishment.photo)
            .circleCrop()
            .into(holder.photoImageView)

        // Define um listener de clique para o item do RecyclerView
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            // Cria um Intent para abrir a ProductsActivity, passando o ID do estabelecimento
            val intent = Intent(context, ProductsActivity::class.java)
            intent.putExtra("establishmentId", establishment.id)
            context.startActivity(intent)
        }
    }

    // Retorna o número total de itens na lista
    override fun getItemCount() = establishments.size
}
