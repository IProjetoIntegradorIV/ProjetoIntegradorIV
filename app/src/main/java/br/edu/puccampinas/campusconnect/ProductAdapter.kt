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

// Adaptador de RecyclerView para exibir a lista de produtos
class ProductAdapter(private val products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Cria a ViewHolder para cada item de produto
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Infla o layout de item_product.xml para cada item da lista
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)  // Retorna o ProductViewHolder associado a esse item
    }

    // Vincula os dados do produto à ViewHolder correspondente
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]  // Obtém o produto na posição específica
        holder.bind(product)  // Chama a função bind() para vincular os dados do produto à ViewHolder

        // Configura o listener de clique para abrir a tela de detalhes do produto
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context  // Obtém o contexto da ViewHolder
            val intent = Intent(context, ProductActivity::class.java)  // Cria uma nova Intent para a ProductActivity
            // Passa os dados do produto para a ProductActivity
            intent.putExtra("productId", product.id)
            intent.putExtra("establishmentId", product.establishmentId)
            intent.putExtra("productName", product.name)
            intent.putExtra("productDescription", product.description)
            intent.putExtra("productEvaluation", product.evaluation)
            intent.putExtra("productPhoto", product.photo)
            intent.putExtra("productPrice", product.price)
            context.startActivity(intent)  // Inicia a ProductActivity com os dados do produto
        }
    }

    // Retorna o número de itens na lista de produtos
    override fun getItemCount(): Int = products.size

    // ViewHolder que exibe os dados de um produto em cada item da RecyclerView
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Função que vincula os dados do produto aos componentes da interface
        fun bind(product: Product) {
            // Vincula os textos dos campos de nome, descrição, avaliação e preço aos respectivos TextViews
            itemView.findViewById<TextView>(R.id.productName).text = product.name
            itemView.findViewById<TextView>(R.id.productDescription).text = product.description
            itemView.findViewById<TextView>(R.id.productEvaluation).text = product.evaluation
            itemView.findViewById<TextView>(R.id.productPrice).text = product.price

            // Vincula a imagem do produto usando o Glide para carregar a imagem e exibi-la em um ImageView
            val imageView = itemView.findViewById<ImageView>(R.id.productPhoto)
            Glide.with(itemView.context)  // Usa o Glide para carregar a imagem do produto
                .load(product.photo)  // Carrega a URL da foto do produto
                .circleCrop()  // Aplica um recorte circular na imagem
                .into(imageView)  // Coloca a imagem no ImageView
        }
    }
}
