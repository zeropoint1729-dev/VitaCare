package com.vitacare.app.data

import android.content.Context
import androidx.room.*
import com.vitacare.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

@Entity(tableName = "diseases")
data class Disease(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val emoji: String,
    val summary: String,
    val symptoms: String,   // newline-separated
    val effects: String,    // newline-separated
    val medication: String  // newline-separated steps
)

@Entity(tableName = "tips")
data class Tip(@PrimaryKey val id: Int, val category: String, val text: String)

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey val id: String,
    val title: String,
    val source: String,
    val category: String,
    val imageUrl: String?,
    val url: String?,
    val publishedAt: String,
    val bookmarked: Boolean = false
)

@Dao
interface DiseaseDao {
    @Query("SELECT * FROM diseases ORDER BY name") fun observe(): Flow<List<Disease>>
    @Query("SELECT * FROM diseases WHERE id = :id") suspend fun byId(id: String): Disease?
    @Query("SELECT COUNT(*) FROM diseases") suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<Disease>)
}

@Dao
interface TipDao {
    @Query("SELECT * FROM tips ORDER BY id") fun observe(): Flow<List<Tip>>
    @Query("SELECT * FROM tips ORDER BY id") suspend fun all(): List<Tip>
    @Query("SELECT COUNT(*) FROM tips") suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<Tip>)
}

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC") fun observe(): Flow<List<Article>>
    @Query("UPDATE articles SET bookmarked = :b WHERE id = :id") suspend fun setBookmark(id: String, b: Boolean)
    @Query("SELECT COUNT(*) FROM articles") suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<Article>)
}

@Database(entities = [Disease::class, Tip::class, Article::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diseaseDao(): DiseaseDao
    abstract fun tipDao(): TipDao
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun instance(ctx: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "vitacare.db")
                .build().also { inst = it }
        }
    }
}

/* ---------- Live news (optional, needs free newsapi.org key) ---------- */

private data class NewsResponse(val articles: List<ApiArticle>?)
private data class ApiArticle(
    val title: String?, val url: String?, val urlToImage: String?,
    val publishedAt: String?, val source: ApiSource?
)
private data class ApiSource(val name: String?)

private interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun top(@retrofit2.http.Query("category") c: String, @retrofit2.http.Query("apiKey") k: String): NewsResponse
}

object NewsRemote {
    suspend fun fetchInto(dao: ArticleDao) {
        val key = BuildConfig.NEWS_API_KEY
        if (key.isBlank()) return
        runCatching {
            val api = Retrofit.Builder()
                .baseUrl(BuildConfig.NEWS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(NewsApi::class.java)
            val res = api.top("health", key)
            val mapped = res.articles.orEmpty().mapNotNull { a ->
                a.url?.let { url ->
                    Article(
                        id = url, title = a.title.orEmpty(),
                        source = a.source?.name ?: "News", category = "World Health",
                        imageUrl = a.urlToImage, url = url, publishedAt = a.publishedAt.orEmpty()
                    )
                }
            }
            if (mapped.isNotEmpty()) dao.insertAll(mapped)
        }
    }
}

/* ---------- Offline seed content ---------- */

object SeedData {
    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.diseaseDao().count() == 0) db.diseaseDao().insertAll(diseases)
        if (db.tipDao().count() == 0) db.tipDao().insertAll(tips)
        if (db.articleDao().count() == 0) db.articleDao().insertAll(articles)
    }

    private val diseases = listOf(
        Disease("diabetes", "Diabetes (Type 2)", "Metabolic", "🍬",
            "A chronic condition where the body cannot regulate blood sugar properly.",
            "Frequent urination\nExcessive thirst and hunger\nUnexplained weight loss\nFatigue and blurred vision\nSlow-healing wounds",
            "Nerve damage (neuropathy)\nKidney disease\nIncreased cardiovascular risk\nPoor wound healing",
            "Start metformin as first-line therapy (as prescribed)\nMonitor blood glucose daily (fasting 80-130 mg/dL)\nAdopt low-sugar diet and 150 min weekly exercise\nReview HbA1c every 3 months (target < 7%)\nAdd insulin or second-line drugs only if doctor advises"),
        Disease("hypertension", "Hypertension", "Cardiovascular", "🫀",
            "Persistently elevated blood pressure, often called the silent killer.",
            "Often no symptoms\nMorning headaches\nShortness of breath\nNosebleeds in severe cases",
            "Increased stroke risk\nHeart muscle strain\nKidney damage\nVision loss",
            "Begin ACE inhibitor or beta-blocker as prescribed\nMeasure BP twice daily and keep a log\nReduce salt to < 5 g/day, limit alcohol\nFollow-up consultation every 4 weeks\nAdjust dosage only under medical supervision"),
        Disease("asthma", "Asthma", "Respiratory", "🫁",
            "Chronic airway inflammation causing reversible breathing obstruction.",
            "Wheezing and coughing (worse at night)\nChest tightness\nShortness of breath on exertion",
            "Sleep disturbance\nLimited physical activity\nRisk of severe attacks",
            "Use reliever inhaler (salbutamol) during symptoms\nTake controller inhaled corticosteroid daily\nIdentify and avoid personal triggers\nFollow a written asthma action plan\nReview inhaler technique at every visit"),
        Disease("migraine", "Migraine", "Neurological", "🧠",
            "Recurrent moderate-to-severe headaches, often one-sided with sensory sensitivity.",
            "Throbbing one-sided headache\nNausea or vomiting\nSensitivity to light and sound\nVisual aura in some patients",
            "Lost productivity and sleep disruption\nDehydration during attacks\nChronic migraine if untreated",
            "Take NSAID or triptan at attack onset\nRest in a dark, quiet room\nMaintain regular sleep and meal schedule\nConsider preventive therapy if > 4 attacks/month\nTrack triggers in a headache diary"),
        Disease("anemia", "Iron-Deficiency Anemia", "Hematologic", "🩸",
            "Low red blood cell count caused by insufficient iron.",
            "Fatigue and weakness\nPale skin and nails\nDizziness and cold hands\nShortness of breath on effort",
            "Reduced oxygen delivery to organs\nHeart strain (tachycardia)\nDelayed growth in children",
            "Take oral ferrous sulfate (as prescribed) with vitamin C\nAdd iron-rich foods: red meat, lentils, spinach\nAvoid tea/coffee within 2 h of doses\nRecheck hemoglobin and ferritin after 8 weeks\nContinue 3 months after levels normalize to refill stores"),
        Disease("dengue", "Dengue Fever", "Infectious", "🦟",
            "Mosquito-borne viral infection common in tropical regions.",
            "Sudden high fever\nSevere headache and pain behind eyes\nJoint and muscle pain\nSkin rash after 3-4 days",
            "Dehydration\nFalling platelet count\nWarning signs of severe dengue (bleeding, abdominal pain)",
            "No specific antiviral - manage supportively\nUse paracetamol only (avoid NSAIDs/aspirin)\nDrink ORS/fluids aggressively to prevent dehydration\nMonitor platelets daily during febrile phase\nHospitalize immediately if warning signs appear")
    )

    private val tips = listOf(
        Tip(1, "Hydration", "Stay hydrated - aim for 8 glasses of water a day."),
        Tip(2, "Activity", "A 30-minute brisk walk daily cuts heart disease risk by up to 30%."),
        Tip(3, "Sleep", "Adults need 7-9 hours of sleep; keep a fixed sleep schedule."),
        Tip(4, "Nutrition", "Fill half your plate with vegetables and fruits at every meal."),
        Tip(5, "Mindfulness", "Ten minutes of mindful breathing lowers cortisol and blood pressure."),
        Tip(6, "Skin", "Apply SPF 30+ sunscreen every morning, even on cloudy days."),
        Tip(7, "Nutrition", "Cut added sugar: swap sodas for water, fruit, or unsweetened tea."),
        Tip(8, "Activity", "Desk job? Stand and stretch for 2 minutes every hour."),
        Tip(9, "Hygiene", "Wash hands with soap for 20 seconds - the cheapest medicine there is."),
        Tip(10, "Prevention", "Book an annual check-up: early detection saves lives.")
    )

    private val articles = listOf(
        Article("a1", "10 Heart-Healthy Eating Habits Backed by Science", "VitaCare Editorial", "Nutrition",
            "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&q=60", null, "2026-09-12"),
        Article("a2", "Strength Training 101: A Beginner's 3-Day Weekly Plan", "VitaCare Fitness", "Fitness",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&q=60", null, "2026-09-11"),
        Article("a3", "Five-Minute Breathing Exercises That Actually Lower Stress", "VitaCare Mind", "Mental Health",
            "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&q=60", null, "2026-09-10"),
        Article("a4", "Understanding Your Blood Pressure Numbers in 2026", "VitaCare Cardiology", "Heart Health",
            "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=400&q=60", null, "2026-09-09"),
        Article("a5", "Sleep Hygiene: The 20-Minute Wind-Down Routine", "VitaCare Mind", "Mental Health",
            "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&q=60", null, "2026-09-08"),
        Article("a6", "Seasonal Influenza 2026: What's New in This Year's Vaccine", "WHO Digest", "World Health",
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&q=60",
            "https://www.who.int/news-room/questions-and-answers/item/influenza-(seasonal)", "2026-09-07")
    )
}
