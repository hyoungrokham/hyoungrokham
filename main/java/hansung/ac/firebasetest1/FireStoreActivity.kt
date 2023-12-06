package hansung.ac.firebasetest1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreActivity : AppCompatActivity() {
    private var adapter: MyAdapter? = null
    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("items")
    private var snapshotListener: ListenerRegistration? = null

    private val checkAutoID by lazy { findViewById<CheckBox>(R.id.checkAutoID) }
    private val editID by lazy { findViewById<EditText>(R.id.editID) }
    private val recyclerViewItems by lazy { findViewById<RecyclerView>(R.id.recyclerViewItems) }
    private val textSnapshotListener by lazy { findViewById<TextView>(R.id.textSnapshotListener) }
    private val editItemName by lazy { findViewById<EditText>(R.id.editItemName)}
    private val editPrice by lazy {findViewById<EditText>(R.id.editPrice)}
    private val progressWait by lazy {findViewById<ProgressBar>(R.id.progressWait)}

    private val editTitle by lazy { findViewById<EditText>(R.id.editTitle) }
    private val editContent by lazy { findViewById<EditText>(R.id.editContent) }
    private val editSellerId by lazy { findViewById<EditText>(R.id.editSellerId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firestore)

        setButtonsVisibility(View.GONE)

        checkAutoID.setOnClickListener {
            editID.isEnabled = !checkAutoID.isChecked
            if (!editID.isEnabled)
                editID.setText("")
        }

        val buttonsVisibility = if (checkAutoID.isChecked) View.GONE else View.VISIBLE
        setButtonsVisibility(buttonsVisibility)

        // recyclerview setup
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(this, emptyList())
        adapter?.setOnItemClickListener {
            queryItemDetails(it)
        }
        recyclerViewItems.adapter = adapter

        updateList()  // list items on recyclerview

        findViewById<Button>(R.id.buttonAddUpdate)?.setOnClickListener {
            addItem()
        }

        findViewById<Button>(R.id.buttonUpdatePrice)?.setOnClickListener {
            updatePrice()
        }

        findViewById<Button>(R.id.buttonIncrPrice)?.setOnClickListener {
            incrPrice()
        }

        findViewById<Button>(R.id.buttonQuery)?.setOnClickListener {
            queryWhere()
        }

        findViewById<Button>(R.id.buttonDelete)?.setOnClickListener {
            deleteItem()
        }

    }

    private fun setButtonsVisibility(visibility: Int) {
        val buttons = arrayOf(
            findViewById<Button>(R.id.buttonAddUpdate),
            findViewById<Button>(R.id.buttonUpdatePrice),
            findViewById<Button>(R.id.buttonIncrPrice),
            findViewById<Button>(R.id.buttonQuery),
            findViewById<Button>(R.id.buttonDelete)
        )

        for (button in buttons) {
            button.visibility = visibility
        }
    }

    private fun queryItemDetails(itemId: String) {
        itemsCollectionRef.document(itemId).get()
            .addOnSuccessListener {
                textSnapshotListener.text = ""  // 기존 내용을 지우고 새로운 내용으로 대체
                textSnapshotListener.append("제목: ${it["title"]}\n")
                textSnapshotListener.append("내용: ${it["content"]}\n")
                textSnapshotListener.append("이름: ${it["name"]}\n")
                textSnapshotListener.append("가격: ${it["price"]}\n")
                textSnapshotListener.append("판매자의 id: ${it["sellerId"]}")
            }.addOnFailureListener {
                // 실패 시 처리
            }
    }

    override fun onStart() {
        super.onStart()

        // snapshot listener for all items
        /*
        snapshotListener = itemsCollectionRef.addSnapshotListener { snapshot, error ->
            textSnapshotListener.text = StringBuilder().apply {
                for (doc in snapshot!!.documentChanges) {
                    append("${doc.type} ${doc.document.id} ${doc.document.data}")
                }
            }
        }
        */
        // sanpshot listener for single item
        /*
        itemsCollectionRef.document("1").addSnapshotListener { snapshot, error ->
            Log.d(TAG, "${snapshot?.id} ${snapshot?.data}")
        }*/
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }

    private fun updateList() {
        itemsCollectionRef.get().addOnSuccessListener {
            val items = mutableListOf<Item>()
            for (doc in it) {
                items.add(Item(doc))
            }
            adapter?.updateList(items)
        }
    }

    private fun addItem() {
        val name = editItemName.text.toString()
        if (name.isEmpty()) {
            Snackbar.make(editItemName, "Input name!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val price = editPrice.text.toString().toInt()

        val title = editTitle.text.toString() // Assuming you have an EditText with id editTitle
        val content = editContent.text.toString() // Assuming you have an EditText with id editContent
        val sellerId = editSellerId.text.toString() // Assuming you have an EditText with id editSellerId

        val autoID = checkAutoID.isChecked
        val itemID = editID.text.toString()

        if (!autoID and itemID.isEmpty()) {
            Snackbar.make(editID, "Input ID or check Auto-generate ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val itemMap = hashMapOf(
            "name" to name,
            "price" to price,
            "title" to title,
            "content" to content,
            "sellerId" to sellerId
        )
        if (autoID) {
            itemsCollectionRef.add(itemMap)
                .addOnSuccessListener { updateList() }.addOnFailureListener {  }
        } else {
            itemsCollectionRef.document(itemID).set(itemMap)
                .addOnSuccessListener { updateList() }.addOnFailureListener {  }
        }
    }

    private fun queryItem(itemID: String) {
        itemsCollectionRef.document(itemID).get()
            .addOnSuccessListener {
                editID.setText(it.id)
                checkAutoID.isChecked = false
                editID.isEnabled = true
                editItemName.setText(it["name"].toString())
                editPrice.setText(it["price"].toString())

                editTitle.setText(it["title"].toString())
                editContent.setText(it["content"].toString())
                editSellerId.setText(it["sellerId"].toString())
            }.addOnFailureListener {
            }
    }

    private fun updatePrice() {
        val itemID = editID.text.toString()
        val price = editPrice.text.toString().toInt()
        if (itemID.isEmpty()) {
            Snackbar.make(editID, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        itemsCollectionRef.document(itemID).update("price", price)
            .addOnSuccessListener { queryItem(itemID) }
    }

    private fun incrPrice() {
        val itemID = editID.text.toString()
        if (itemID.isEmpty()) {
            Snackbar.make(editID, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }

        db.runTransaction {
            val docRef = itemsCollectionRef.document(itemID)
            val snapshot = it.get(docRef)
            var price = snapshot.getLong("price") ?: 0
            price += 1
            it.update(docRef, "price", price)
        }
            .addOnSuccessListener { queryItem(itemID) }
    }

    private fun queryWhere() {
        val p = 100
        progressWait.visibility = View.VISIBLE
        itemsCollectionRef.whereLessThan("price", p).get()
            .addOnSuccessListener {
                progressWait.visibility = View.GONE
                val items = arrayListOf<String>()
                for (doc in it) {
                    items.add("${doc["name"]} - ${doc["price"]}")
                }
                AlertDialog.Builder(this)
                    .setTitle("Items which price less than $p")
                    .setItems(items.toTypedArray(), { dialog, which ->  }).show()
            }
            .addOnFailureListener {
                progressWait.visibility = View.GONE
            }
    }

    private fun deleteItem() {
        val itemID = editID.text.toString()
        if (itemID.isEmpty()) {
            Snackbar.make(editID, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        itemsCollectionRef.document(itemID).delete()
            .addOnSuccessListener { updateList() }
    }

    companion object {
        const val TAG = "FirestoreActivity"
    }
}
