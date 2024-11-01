    package br.univali.contactsapplication

    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.recyclerview.widget.LinearLayoutManager
    import br.univali.contactsapplication.databinding.ActivityMainBinding

    class MainActivity : AppCompatActivity() {

        private lateinit var binding: ActivityMainBinding
        private lateinit var db: ContactDatabaseHelper
        private lateinit var contactAdapter: ContactsAdapter


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            db = ContactDatabaseHelper(this)
            contactAdapter = ContactsAdapter(db.getAllContacts(), this)

            binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.notesRecyclerView.adapter = contactAdapter

            binding.addButton.setOnClickListener { it: View ->
                val intent = Intent(this, AddContactActivity::class.java)
                startActivity(intent)
            }
        }

        override fun onResume(){
            super.onResume()
            contactAdapter.refreshData(db.getAllContacts())
        }
    }
