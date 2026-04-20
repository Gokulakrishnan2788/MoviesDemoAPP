# Database Contract (Room)
# All Room setup lives in :core:data

## AppDatabase
@Database(entities = [WatchlistEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}

## WatchlistEntity
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val imdbID: String,
    val title: String,
    val year: String,
    val posterUrl: String,
    val rating: String,
    val genre: String,
    val addedAt: Long = System.currentTimeMillis()
)

## WatchlistDao
@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Delete
    suspend fun delete(entity: WatchlistEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbID = :id)")
    suspend fun isInWatchlist(id: String): Boolean
}

## DataModule (Hilt)
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "movieapp.db").build()

    @Provides @Singleton
    fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()
}
