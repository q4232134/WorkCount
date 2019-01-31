package com.jiaozhu.workcount

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jiaozhu.workcount.data.AppDatabase
import com.jiaozhu.workcount.data.PrefSupport
import java.util.*


/**
 * Created by 教主 on 2017/12/18.
 */
public class CApplication : Application() {
    lateinit var db: AppDatabase
    var serviceStartTime: Date = Date()

    override fun onCreate() {
        super.onCreate()
        PrefSupport.context = this
        db = Room.databaseBuilder(this, AppDatabase::class.java, "database").allowMainThreadQueries()
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
        }
    }
}