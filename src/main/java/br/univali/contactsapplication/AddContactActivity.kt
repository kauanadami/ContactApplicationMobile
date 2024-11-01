package br.univali.contactsapplication

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import br.univali.contactsapplication.databinding.ActivityAddContactBinding

class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private lateinit var db: ContactDatabaseHelper
    private var phoneFieldList = mutableListOf<Triple<EditText, Spinner, ImageView>>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = ContactDatabaseHelper(this)

        addPhoneField()

        binding.addPhoneButton.setOnClickListener {
            addPhoneField()
        }

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()

            val phoneList = phoneFieldList.mapNotNull { triple ->
                val phoneText = unmask(triple.first.text.toString().trim())
                val typeText = triple.second.selectedItem.toString()

                if (phoneText.length == 11 && typeText != "Selecionar") {
                    Phone(0, 0, phoneText, typeText)
                } else {
                    null
                }
            }

            if (title.isBlank()) {
                Toast.makeText(this, "Por favor, insira o nome do contato", Toast.LENGTH_SHORT).show()
            } else if (phoneList.isEmpty()) {
                Toast.makeText(this, "Adicione pelo menos um número de telefone válido com 11 dígitos", Toast.LENGTH_SHORT).show()
            } else {
                val contact = Contact(0, title, phoneList)
                db.insertContact(contact)
                finish()
                Toast.makeText(this, "Contato salvo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addPhoneField() {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.HORIZONTAL
        container.gravity = Gravity.CENTER_VERTICAL

        val heightInDp = 60
        val heightInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightInDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val phoneEditText = EditText(this)
        phoneEditText.hint = "Digite o número"
        phoneEditText.textSize = 18f
        phoneEditText.setPadding(12, 0, 12, 0)
        phoneEditText.background = ContextCompat.getDrawable(this, R.drawable.orange_border)
        phoneEditText.typeface = ResourcesCompat.getFont(this, R.font.poppins)
        phoneEditText.inputType = InputType.TYPE_CLASS_NUMBER

        // Adicionar o TextWatcher para máscara
        phoneEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "(##) #####-####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) {
                    return
                }

                val str = unmask(s.toString())
                if (str.length > 11) {
                    s?.delete(s.length - 1, s.length)
                    return
                }

                var formatted = ""
                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#' && str.length > i) {
                        formatted += m
                    } else {
                        try {
                            formatted += str[i]
                        } catch (e: Exception) {
                            break
                        }
                        i++
                    }
                }
                isUpdating = true
                phoneEditText.setText(formatted)
                phoneEditText.setSelection(formatted.length)
                isUpdating = false
            }
        })

        val phoneParams = LinearLayout.LayoutParams(
            0,
            heightInPx,
            2f
        )
        phoneParams.setMargins(0, 16, 8, 16)
        phoneEditText.layoutParams = phoneParams

        val typeSpinner = Spinner(this)
        val types = arrayOf("Selecionar", "Pessoal", "Comercial", "Residencial")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter
        typeSpinner.background = ContextCompat.getDrawable(this, R.drawable.orange_border)
        typeSpinner.minimumHeight = heightInPx
        val typeParams = LinearLayout.LayoutParams(
            0,
            heightInPx,
            1.5f
        )
        typeParams.setMargins(8, 16, 8, 16)
        typeSpinner.layoutParams = typeParams

        // Delete Button
        val deleteButton = ImageView(this)
        deleteButton.setImageResource(R.drawable.baseline_delete_24)
        deleteButton.setColorFilter(ContextCompat.getColor(this, R.color.orange))
        val deleteParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        deleteParams.setMargins(8, 16, 0, 16)
        deleteButton.layoutParams = deleteParams

        // Set OnClickListener for Delete Button
        deleteButton.setOnClickListener {
            binding.phoneContainer.removeView(container)
            phoneFieldList.removeIf { it.first == phoneEditText }
        }

        container.addView(phoneEditText)
        container.addView(typeSpinner)
        container.addView(deleteButton)
        binding.phoneContainer.addView(container)
        phoneFieldList.add(Triple(phoneEditText, typeSpinner, deleteButton))
    }

    private fun unmask(s: String): String {
        return s.replace("[^\\d]".toRegex(), "")
    }
}
