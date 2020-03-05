package ru.otus.demo.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class SimpleBgTask(
        context: Context,
        workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("WorkManager", this.javaClass.simpleName + " works, id:" + id)
        for (i in 0..9) {
            if (isStopped) {
                Log.d("WorkManager", "isStopped()")
                break
            }
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        Log.d("WorkManager", this.javaClass.simpleName + " finished, id:" + id)
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        Log.d("WorkManager", "onStopped()")
    }
}