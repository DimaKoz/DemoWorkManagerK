package ru.otus.demo.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class DataBgTask(
        context: Context,
        workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("WorkManager", this.javaClass.simpleName + " works, id:" + id)
        val inputStr = inputData.getString("keyString")
        val inputInt = inputData.getInt("keyInt", 0)
        val strOutput = this.javaClass.simpleName + " got, string:[" + inputStr + "], int:[" + inputInt + "]"
        Log.d("WorkManager", strOutput)
        val output = Data.Builder()
                .putString("keyString", strOutput)
                .build()
        Log.d("WorkManager", this.javaClass.simpleName + " finished, id:" + id)
        return Result.success(output)
    }

    override fun onStopped() {
        super.onStopped()
        Log.d("WorkManager", this.javaClass.simpleName + "onStopped()")
    }
}