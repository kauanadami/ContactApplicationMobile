package br.univali.contactsapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(private var contacts: List<Contact>, context: Context) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    private val db: ContactDatabaseHelper = ContactDatabaseHelper(context)

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.titleTextView.text = contact.title
        holder.contentTextView.text = contact.phones.joinToString(", ") { "${formatPhoneNumber(it.phone)} (${it.type})" }

        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateContactActivity::class.java).apply {
                putExtra("contact_id", contact.id)
            }

            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            db.deleteContact(contact.id)
            refreshData(db.getAllContacts())
            Toast.makeText(holder.itemView.context, "Contato excluído", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshData(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    private fun formatPhoneNumber(phone: String): String {
        val mask = "(##) #####-####"
        val unmaskedPhone = phone.replace("[^\\d]".toRegex(), "")
        var formatted = ""
        var index = 0

        for (char in mask.toCharArray()) {
            if (char == '#') {
                if (index < unmaskedPhone.length) {
                    formatted += unmaskedPhone[index]
                    index++
                } else {
                    break
                }
            } else {
                formatted += char
            }
        }

        return formatted
    }
}
