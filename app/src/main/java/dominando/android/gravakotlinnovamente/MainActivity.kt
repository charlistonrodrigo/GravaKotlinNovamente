package dominando.android.gravakotlinnovamente

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.System.load

@Suppress("DEPRECATION")
//@RuntimePermissions
class MainActivity : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.FROYO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRead.setOnClickListener {
            btnReadClick()
        }
        btnSave.setOnClickListener {
            btnSaveClick()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            RC_STORAGE_PERMISSION -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkStoragePermission(permission: String, requestCode: Int): Boolean{

        if(ActivityCompat.checkSelfPermission(this, permission)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                Toast.makeText(this, R.string.message_permission_requested,
                    Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun btnReadClick() {
        val type = rgType.checkedRadioButtonId
        when (type) {
            R.id.rbInternal -> loadFromInternal()
            R.id.rbExternalPriv -> loadFromExternal(true)
            R.id.rbExternalPublic -> loadFromExternal(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun btnSaveClick() {
        val type = rgType.checkedRadioButtonId
        when (type) {
            R.id.rbInternal -> saveToInternal()
            R.id.rbExternalPriv -> saveToExternal(true)
            R.id.rbExternalPublic -> saveToExternal(false)
        }
    }

    private fun saveToInternal() {
        try {
            val fos = openFileOutput("arquivo.txt", Context.MODE_PRIVATE)
            save(fos)
        } catch (e: Exception) {
            Log.e("NGVL", "Erro ao salvar o arquivo", e)
        }
    }

    private fun loadFromInternal() {
        try {
            val fis = openFileInput("arquivo.txt")
            load(fis)
        } catch (e: Exception) {
            Log.e("NGVL", "Erro ao carregar o arquivo", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun getExternalDir(privateDir: Boolean) =
        // SDCard/Android/data/pacote.da.app/files
        if(privateDir) getExternalFilesDir(null)
        // SDCard/DCIM
        else Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM)

    @RequiresApi(Build.VERSION_CODES.FROYO)
    // @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun saveToExternal(privateDir: Boolean){

        val hasPermission = checkStoragePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            RC_STORAGE_PERMISSION)
        if(!hasPermission){
            return
        }

        val state = Environment.getExternalStorageState()

        if(Environment.MEDIA_MOUNTED == state){
            val myDir = getExternalDir(privateDir)

            try{
                if(myDir?.exists() == false){
                    myDir.mkdir()
                }
                val txtFile = File(myDir, "arquivo.txt")
                if(!txtFile.exists()){
                    txtFile.createNewFile()
                }
                val fos = FileOutputStream(txtFile)
                save(fos)
            } catch(e : IOException){
                Log.d("NGVL", "Erro ao salvar o arquivo", e)
            }
        } else{
            Log.e("NGVL", "Não é possivel escrever no SD Card")
        }
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    // @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun loadFromExternal(privateDir: Boolean){

        val hasPermission = checkStoragePermission(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            RC_STORAGE_PERMISSION)
        if(!hasPermission){
            return
        }

        val state = Environment.getExternalStorageState()

        if(Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state){

            val myDir = getExternalDir(privateDir)
            if(myDir?.exists() == true){
                val txtFile = File(myDir, "arquivo.txt")
                if((txtFile.exists())){

                    try{
                        txtFile.createNewFile()
                        val fis = FileInputStream(txtFile)
                        load(fis)
                    } catch(e: IOException){
                        Log.d("NGVL", "Erro ao carregar arquivo", e)
                    }
                }
            }
        } else{
            Log.e("NGVL", "SD Card indisponível")
        }
    }


    private fun save(fos: FileOutputStream) {
        val lines = TextUtils.split(edtText.text.toString(), "\n")
        val writer = PrintWriter(fos)
        for (line in lines) {
            writer.println(line)
        }
        writer.flush()
        writer.close()
        fos.close()
    }

    private fun load(fis: FileInputStream) {
        val reader = BufferedReader(InputStreamReader(fis))
        val sb = StringBuilder()
        do {
            val line = reader.readLine() ?: break
            if (sb.isNotEmpty()) sb.append("\n")
            sb.append(line)
        } while (true)
        reader.close()
        fis.close()
        txtText.text = sb.toString()
    }

    companion object{
        val RC_STORAGE_PERMISSION = 0
    }

}


