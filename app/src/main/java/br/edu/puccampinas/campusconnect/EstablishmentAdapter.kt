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

class EstablishmentAdapter(private val establishments: List<Establishment>) :
    RecyclerView.Adapter<EstablishmentAdapter.EstablishmentViewHolder>() {

    class EstablishmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
        val openingHoursTextView: TextView = view.findViewById(R.id.openingHoursTextView)
        val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstablishmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_establishment, parent, false)
        return EstablishmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstablishmentViewHolder, position: Int) {
        val establishment = establishments[position]
        holder.nameTextView.text = establishment.name
        holder.descriptionTextView.text = establishment.description
        holder.openingHoursTextView.text = establishment.openingHours
        Glide.with(holder.itemView.context).load(establishment.photo).circleCrop().into(holder.photoImageView)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductsActivity::class.java)
            intent.putExtra("establishmentId", establishment.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = establishments.size
}
