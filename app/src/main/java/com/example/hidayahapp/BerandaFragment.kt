package com.example.hidayahapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BerandaFragment : Fragment() {

    private lateinit var fab: FloatingActionButton
    private lateinit var textviewSubuh: TextView
    private lateinit var textviewSunrise: TextView
    private lateinit var textviewDzuhur: TextView
    private lateinit var textviewAshar: TextView
    private lateinit var textviewMaghrib: TextView
    private lateinit var textviewIsya: TextView
    private lateinit var textviewTanggal: TextView
    private lateinit var buttonPrevDay: ImageButton
    private lateinit var buttonNextDay: ImageButton
    private lateinit var profile: ImageButton

    private val calendar = Calendar.getInstance()

    private fun getFormattedData(): String {
        val format =
            SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        return format.format(calendar.time)
    }

    private fun getApiDateFormatted(): String {
        val format =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(calendar.time)
    }

    private fun fetchWaktuShalat(date: String) {
        val url =
            "https://api.aladhan.com/v1/timingsByAddress/$date?address=Yogyakarta&method=2"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val timings =
                        response.getJSONObject("data")
                            .getJSONObject("timings")

                    val fajr = timings.getString("Fajr")
                    val sunrise = timings.getString("Sunrise")
                    val dhuhr = timings.getString("Dhuhr")
                    val asr = timings.getString("Asr")
                    val maghrib = timings.getString("Maghrib")
                    val isha = timings.getString("Isha")

                    textviewSubuh.text = fajr
                    textviewSunrise.text = sunrise
                    textviewDzuhur.text = dhuhr
                    textviewAshar.text = asr
                    textviewMaghrib.text = maghrib
                    textviewIsya.text = isha
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        Volley
            .newRequestQueue(requireContext())
            .add(request)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view =
            inflater.inflate(R.layout.fragment_beranda, container, false)

        textviewSubuh = view.findViewById(R.id.waktuViewShubuh)
        textviewSunrise = view.findViewById(R.id.waktuViewSunrise)
        textviewDzuhur = view.findViewById(R.id.waktuViewDzuhur)
        textviewAshar = view.findViewById(R.id.waktuViewAshar)
        textviewMaghrib = view.findViewById(R.id.waktuViewMagrib)
        textviewIsya = view.findViewById(R.id.waktuViewIsya)
        textviewTanggal = view.findViewById(R.id.textViewDate)
        buttonPrevDay = view.findViewById(R.id.buttonPrevDay)
        buttonNextDay = view.findViewById(R.id.buttonNextDay)
        profile = view.findViewById(R.id.imgProfile)
        fab = view.findViewById(R.id.fab)

        // Initial data
        textviewTanggal.text = getFormattedData()
        fetchWaktuShalat(getApiDateFormatted())

        buttonPrevDay.setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            textviewTanggal.text = getFormattedData()
            fetchWaktuShalat(getApiDateFormatted())
        }

        buttonNextDay.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            textviewTanggal.text = getFormattedData()
            fetchWaktuShalat(getApiDateFormatted())
        }

        profile.setOnClickListener {
            val intent =
                Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        fab.setOnClickListener {
            val intent =
                Intent(requireContext(), ListCatatanActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(): BerandaFragment {
            return BerandaFragment()
        }
    }
}
