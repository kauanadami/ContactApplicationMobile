package br.univali.contactsapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contactsapp.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_CONTACTS = "contacts"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"

        private const val TABLE_PHONES = "phones"
        private const val COLUMN_PHONE_ID = "phone_id"
        private const val COLUMN_CONTACT_ID = "contact_id"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_TYPE = "type"
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createContactsTableQuery = """
            CREATE TABLE $TABLE_CONTACTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT
            )
        """.trimIndent()
        db?.execSQL(createContactsTableQuery)

        val createPhonesTableQuery = """
            CREATE TABLE $TABLE_PHONES (
                $COLUMN_PHONE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CONTACT_ID INTEGER,
                $COLUMN_PHONE TEXT,
                $COLUMN_TYPE TEXT,
                FOREIGN KEY($COLUMN_CONTACT_ID) REFERENCES $TABLE_CONTACTS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db?.execSQL(createPhonesTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropPhonesTableQuery = "DROP TABLE IF EXISTS $TABLE_PHONES"
        val dropContactsTableQuery = "DROP TABLE IF EXISTS $TABLE_CONTACTS"
        db?.execSQL(dropPhonesTableQuery)
        db?.execSQL(dropContactsTableQuery)
        onCreate(db)
    }

    fun insertContact(contact: Contact) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val contactValues = ContentValues().apply {
                put(COLUMN_TITLE, contact.title)
            }
            val contactId = db.insert(TABLE_CONTACTS, null, contactValues)
            if (contactId != -1L) {
                for (phone in contact.phones) {
                    val phoneValues = ContentValues().apply {
                        put(COLUMN_CONTACT_ID, contactId)
                        put(COLUMN_PHONE, phone.phone)
                        put(COLUMN_TYPE, phone.type)
                    }
                    db.insert(TABLE_PHONES, null, phoneValues)
                }
                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getAllContacts(): List<Contact> {
        val contactsList = mutableListOf<Contact>()
        val db = readableDatabase
        val contactsCursor = db.query(
            TABLE_CONTACTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TITLE COLLATE NOCASE ASC"
        )
        while (contactsCursor.moveToNext()) {
            val id = contactsCursor.getInt(contactsCursor.getColumnIndexOrThrow(COLUMN_ID))
            val title = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val phones = getPhonesByContactId(id)
            val contact = Contact(id, title, phones)
            contactsList.add(contact)
        }
        contactsCursor.close()
        db.close()
        return contactsList
    }

    private fun getPhonesByContactId(contactId: Int): List<Phone> {
        val phonesList = mutableListOf<Phone>()
        val db = readableDatabase
        val selection = "$COLUMN_CONTACT_ID = ?"
        val selectionArgs = arrayOf(contactId.toString())
        val cursor = db.query(TABLE_PHONES, null, selection, selectionArgs, null, null, null)
        while (cursor.moveToNext()) {
            val phoneId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PHONE_ID))
            val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
            val phoneType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
            val phone = Phone(phoneId, contactId, phoneNumber, phoneType)
            phonesList.add(phone)
        }
        cursor.close()
        return phonesList
    }

    fun getContactByID(contactId: Int): Contact? {
        val db = readableDatabase
        val selection = "$COLUMN_ID = ?"
        val selectionArgs = arrayOf(contactId.toString())
        val cursor = db.query(TABLE_CONTACTS, null, selection, selectionArgs, null, null, null)
        var contact: Contact? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val phones = getPhonesByContactId(id)
            contact = Contact(id, title, phones)
        }
        cursor.close()
        db.close()
        return contact
    }

    fun updateContact(contact: Contact) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val contactValues = ContentValues().apply {
                put(COLUMN_TITLE, contact.title)
            }
            val whereClause = "$COLUMN_ID = ?"
            val whereArgs = arrayOf(contact.id.toString())
            db.update(TABLE_CONTACTS, contactValues, whereClause, whereArgs)

            // Delete existing phones
            db.delete(TABLE_PHONES, "$COLUMN_CONTACT_ID = ?", whereArgs)

            // Insert new phones
            for (phone in contact.phones) {
                val phoneValues = ContentValues().apply {
                    put(COLUMN_CONTACT_ID, contact.id)
                    put(COLUMN_PHONE, phone.phone)
                    put(COLUMN_TYPE, phone.type)
                }
                db.insert(TABLE_PHONES, null, phoneValues)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun deleteContact(contactId: Int) {
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(contactId.toString())
        db.delete(TABLE_CONTACTS, whereClause, whereArgs)
        db.close()
    }
}
