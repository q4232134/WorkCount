package com.jiaozhu.workcount.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import java.util.*


/**
 * Created by 教主 on 2017/12/18.
 */
@Database(entities = [History::class], version = 2)
@TypeConverters(value = [Converters::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun replace(t: T): Long


    @Delete
    fun delete(vararg beans: T)


    @Delete
    fun delete(beans: List<T>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(t: T)


}

@Dao
interface HistoryDao : BaseDao<History> {
    /**
     * 获取列表
     */
    @Query("select * from History order by createTime desc")
    fun getList(): DataSource.Factory<Int, History>

    @Query("select * from History where id = (select max(id) from History)")
    fun getLastNode(): History

    @Query("select * from History where id = (select max(id) from History)")
    fun getLastNodeLive(): LiveData<History>;

    /**
     * 获取当前wifi名称
     */
    @Query("select ssid from History where id = (select max(id) from History)")
    fun getLastNodeName(): LiveData<String>

    @Query("select * from History where createTime between :start and :end")
    fun getNodeByTime(start: Date, end: Date): LiveData<List<History>>

}


@Entity(tableName = "History")
data class History(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var ssid: String = "",
    var createTime: Date = Date(),
    var isTarget: Boolean = false//是否为目标wifi
)


data class WorkCount(
    val ssid: String,
    val startTime: Date,
    val endTime: Date,
    val length: Long = endTime.time - startTime.time,
    val des: String = ssid.ssidDes
)


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

