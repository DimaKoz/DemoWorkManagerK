package ru.otus.demo.workmanager

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var id: UUID? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            id?.apply {
                WorkManager
                        .getInstance(this@MainActivity)
                        .cancelWorkById(this)
            }

        }

        //Остановить все задачи

/*
        WorkManager
                .getInstance(this).cancelAllWork()
*/

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_simple) {
            startSimpleWorker()
            return true
        }
        if (id == R.id.action_periodic) {
            startPeriodicWorker()
            return true
        }
        if (id == R.id.action_chain) {
            chainWorkers()
            return true
        }
        if (id == R.id.action_data) {
            dataIO()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startSimpleWorkerWithTags() {
        val request = OneTimeWorkRequest.Builder(SimpleBgTask::class.java)
                .addTag("tag1")
                .addTag("tag2")
                .addTag("tag...")
                .build()
        WorkManager
                .getInstance(this)
                .enqueue(request)
        WorkManager
                .getInstance(this)
                .cancelAllWorkByTag("tag2")
    }

    private fun dataIO() {
        val inputData = Data.Builder()
                .putInt("keyInt", 42)
                .putString("keyString", "striiiing")
                .build()
        val request = OneTimeWorkRequest.Builder(DataBgTask::class.java)
                .setInputData(inputData)
                .addTag("tag1")
                .build()
        id = request.id
        WorkManager
                .getInstance(this)
                .getWorkInfoByIdLiveData(id!!)
                .observe(this, Observer { wInfo ->
                    Log.d(TAG, "onChanged: " + wInfo.state)
                    if (wInfo.state == WorkInfo.State.SUCCEEDED) {
                        val output = wInfo.outputData
                        val returned = output.getString("keyString")
                        Log.d(TAG, "keyString:[$returned]")
                    }
                })
        WorkManager
                .getInstance(this)
                .enqueue(request)
    }

    private fun startSimpleWorker() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
        val request = OneTimeWorkRequest.Builder(SimpleBgTask::class.java)
                .addTag("tag1")
                .addTag("tag2")
                .addTag("tag3")
                .setConstraints(constraints)
                .build()

        //Наблюдение за статусом работы
        WorkManager
                .getInstance(this)
                .getWorkInfoByIdLiveData(request.id)
                .observe(this, Observer { wInfo -> Log.d(TAG, "onChanged: " + wInfo.state) })
        id = request.id
        WorkManager
                .getInstance(this)
                .enqueue(request)
    }

    private fun chainWorkers() {
        val workA1 = OneTimeWorkRequest.Builder(SimpleBgTask::class.java)
                .addTag("tag1")
                .build()
        val workA2 = OneTimeWorkRequest.Builder(SimpleBgTask2::class.java)
                .addTag("tag1")
                .build()
        val workB1 = OneTimeWorkRequest.Builder(SimpleBgTask::class.java)
                .addTag("tag1")
                .build()
        val workB2 = OneTimeWorkRequest.Builder(SimpleBgTask2::class.java)
                .addTag("tag1")
                .build()
        val workC = OneTimeWorkRequest.Builder(SimpleBgTask3::class.java)
                .addTag("tag1")
                .build()
        val manager = WorkManager.getInstance(this)
        val chain1 = manager
                .beginWith(workA1)
                .then(workA2)
        val chain2 = manager
                .beginWith(workB1)
                .then(workB2)
        val chain3 = WorkContinuation
                .combine(listOf(chain1, chain2))
                .then(workC)
        chain3.enqueue()
    }

    private fun startPeriodicWorker() {


        val refreshWork = PeriodicWorkRequest.Builder(
                        SimpleBgTask::class.java,
                        30, TimeUnit.MINUTES,
                        15, TimeUnit.MINUTES
                )
                .addTag("tag1")
                .build()
        id = refreshWork.id
        WorkManager.getInstance(this).enqueue(refreshWork)
    }

    companion object {
        const val TAG = "WorkManager"
    }
}