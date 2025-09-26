package com.example.fitconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.adapters.CalendarAdapter
import com.example.fitconnect.adapters.TimeSlotAdapter
import com.example.fitconnect.models.Appointment
import com.example.fitconnect.models.CalendarDay
import com.example.fitconnect.models.TimeSlot
import com.example.fitconnect.services.AppointmentService
import com.example.fitconnect.services.LocalPrefsService
import com.example.fitconnect.services.PatientService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView
import android.widget.TextView
import com.example.fitconnect.models.Notification
import com.example.fitconnect.services.NotificationService

class CreateBookingActivity : AppCompatActivity() {
    private lateinit var appointmentService: AppointmentService
    private lateinit var localPrefsService: LocalPrefsService
    private  lateinit var notificationService: NotificationService
    private lateinit var patientService: PatientService
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    private var doctorId: String? = null
    private var doctorName: String = ""
    private var patientId: String? = null
    private var patientName: String = ""
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_booking)

        initServices()
        getIntentData()
        setupViews()
        loadPatientData()
        setupCalendar()
        setupTimeSlots()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initServices() {
        appointmentService = AppointmentService()
        localPrefsService = LocalPrefsService(this)
        patientService = PatientService()
        notificationService=NotificationService()
        patientId = localPrefsService.getPatientId()
    }

    private fun getIntentData() {
        doctorId = intent.getStringExtra("doctorId")
        doctorName = intent.getStringExtra("doctorName") ?: ""
    }

    private fun setupViews() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.confirmButton).setOnClickListener {
            confirmBooking()
        }

        findViewById<ImageView>(R.id.prevMonthButton).setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        findViewById<ImageView>(R.id.nextMonthButton).setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    private fun loadPatientData() {
        patientId?.let { id ->
            lifecycleScope.launch {
                try {
                    val patient = patientService.getPatientById(id)
                    patient?.let {
                        patientName = it.name
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupCalendar() {
        val recyclerView = findViewById<RecyclerView>(R.id.calendarRecyclerView)
        calendarAdapter = CalendarAdapter { day ->
            if (day.isSelectable) {
                selectedDate = day.date
                calendarAdapter.selectDay(day)
                timeSlotAdapter.clearSelection()
                selectedTime = ""
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 7)
        recyclerView.adapter = calendarAdapter

        updateCalendar()
    }

    private fun setupTimeSlots() {
        val recyclerView = findViewById<RecyclerView>(R.id.timeSlotRecyclerView)
        timeSlotAdapter = TimeSlotAdapter { timeSlot ->
            selectedTime = timeSlot.time
            timeSlotAdapter.selectTime(timeSlot)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = timeSlotAdapter

        val timeSlots = listOf(
            TimeSlot("09.00 AM", false),
            TimeSlot("09.30 AM", false),
            TimeSlot("10.00 AM", true),
            TimeSlot("10.30 AM", false),
            TimeSlot("11.00 AM", false),
            TimeSlot("11.30 AM", false),
            TimeSlot("3.00 PM", false),
            TimeSlot("3.30 PM", false),
            TimeSlot("4.00 PM", false),
            TimeSlot("4.30 PM", false),
            TimeSlot("5.00 PM", false),
            TimeSlot("5.30 PM", false)
        )
        timeSlotAdapter.submitList(timeSlots)
    }

    private fun updateCalendar() {
        findViewById<TextView>(R.id.monthYearText).text = dateFormat.format(calendar.time)

        val calendarDays = generateCalendarDays()
        calendarAdapter.submitList(calendarDays)
    }

    private fun generateCalendarDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val today = Calendar.getInstance()
        val threeMonthsFromNow = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }

        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1

        // Add empty days for previous month
        for (i in 0 until firstDayOfWeek) {
            days.add(CalendarDay("", "", false, false, false))
        }

        val maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..maxDay) {
            tempCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateString = dayFormat.format(tempCalendar.time)
            val dayString = day.toString()

            val isToday = tempCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    tempCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

            val isSelectable = tempCalendar.after(today) || isToday
            val isWithinThreeMonths = tempCalendar.before(threeMonthsFromNow) || tempCalendar == threeMonthsFromNow

            days.add(CalendarDay(
                day = dayString,
                date = dateString,
                isToday = isToday,
                isSelected = false,
                isSelectable = isSelectable && isWithinThreeMonths
            ))
        }

        return days
    }

    private fun confirmBooking() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        if (patientId == null) {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (doctorId == null) {
            Toast.makeText(this, "Doctor ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val appointment = Appointment(
                    status = "Pending",
                    date = selectedDate,
                    hour = selectedTime,
                    doctorId = doctorId!!,
                    patientId = patientId!!,
                    doctorName = doctorName,
                    patientName = patientName
                )
                val notification:Notification = Notification(
                    patientId = patientId!!,
                    snapshotId = appointment.id,
                    title = "Appointment Success",
                    subtitle = "You have successfully booked your appointment with $doctorName",
                )

                appointmentService.createAppointment(appointment)
                notificationService.createNotification(notification)

                Toast.makeText(this@CreateBookingActivity, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@CreateBookingActivity, "Failed to book appointment", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}