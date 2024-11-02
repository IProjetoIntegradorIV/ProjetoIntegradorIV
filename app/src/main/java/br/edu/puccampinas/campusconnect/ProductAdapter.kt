package br.edu.puccampinas.campusconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.puccampinas.campusconnect.data.model.Product
import com.bumptech.glide.Glide

class ProductAdapter(private val products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductActivity::class.java)
            intent.putExtra("productId",product.id)
            intent.putExtra("establishmentId",product.establishmentId)
            intent.putExtra("productName",product.name)
            intent.putExtra("productDescription",product.description)
            intent.putExtra("productEvaluation",product.evaluation)
            intent.putExtra("productPhoto",product.photo)
            intent.putExtra("productPrice",product.price)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(product: Product) {
            itemView.findViewById<TextView>(R.id.productName).text = product.name
            itemView.findViewById<TextView>(R.id.productDescription).text = product.description
            itemView.findViewById<TextView>(R.id.productEvaluation).text = product.evaluation
            itemView.findViewById<TextView>(R.id.productPrice).text = product.price
            val imageView = itemView.findViewById<ImageView>(R.id.productPhoto)
            Glide.with(itemView.context).load(product.photo).circleCrop().into(imageView)
        }
    }
}

