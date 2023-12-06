package hansung.ac.firebasetest1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot

data class Item(val id: String,
                val name: String,
                val price: Int,
                val title: String,
                val content: String,
                val sellerId: String) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id,
                doc["name"].toString(),
                doc["price"].toString().toIntOrNull() ?: 0,
                doc["title"].toString(),
                doc["content"].toString(),
                doc["sellerId"].toString())
    constructor(key: String, map: Map<*, *>) :
            this(key, map["name"].toString(),
                map["price"].toString().toIntOrNull() ?: 0,
                map["title"].toString(),
                map["content"].toString(),
                map["sellerId"].toString()
            )
}

class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

class MyAdapter(private val context: Context, private var items: List<Item>)
    : RecyclerView.Adapter<MyViewHolder>() {

    fun interface OnItemClickListener {
        fun onItemClick(student_id: String)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<Item>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]
        holder.view.findViewById<TextView>(R.id.textID).text = item.id
        holder.view.findViewById<TextView>(R.id.textName).text = item.name
        holder.view.setOnClickListener {
            itemClickListener?.onItemClick(item.id)
        }

    }


    override fun getItemCount() = items.size
}