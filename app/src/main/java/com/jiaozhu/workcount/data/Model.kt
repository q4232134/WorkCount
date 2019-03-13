package com.jiaozhu.workcount.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import java.text.SimpleDateFormat
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

    /**
     *通过指定目标来统计目标时长
     */
    @Query(
        """
            SELECT
        *,
        min( createTime ) AS startTime,
        (
        SELECT
            min( b.createTime )
        FROM
            History b
        WHERE
        b.createTime > max( a.createTime )) AS endTime
    FROM
        History a
    WHERE
        ssid IN  ( :target )
        and createTime between :start and :end
    GROUP BY
        date( createTime / 1000, 'unixepoch', '+8 hours' )
    """
    )
    fun getDailyCount(start: Date, end: Date, target: List<String>): List<WorkCount>

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
    val endTime: Date?,
    val length: Long? = null//持续时长
) {
    fun getEnd(): Date = endTime ?: Date()
    fun getLength(): Long = length ?: getEnd().time - startTime.time
    val des: String get() = ssid.ssidDes
    val workLength: Long
        get() {
            //午休时间
            val sleepTime =
                if (apFormat.format(startTime).toInt() <= 12 &&
                    apFormat.format(getEnd()).toInt() >= 14
                ) (2 * 60 * 60 * 1000) else 0
            return getEnd().time - startTime.time - sleepTime
        }

    companion object {
        val apFormat = SimpleDateFormat("HH", Locale.CHINA)
    }
}


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

