package com.boostcamp.mountainking.ui.tracking.history

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boostcamp.mountainking.R
import com.boostcamp.mountainking.databinding.FragmentHistoryDetailsBinding
import com.boostcamp.mountainking.ui.tracking.LocationService
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PathOverlay


class HistoryDetailsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentHistoryDetailsBinding
    private val args: HistoryDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mvNaver.onCreate(savedInstanceState)
        binding.mvNaver.getMapAsync(this)
        initView()
    }

    private fun initView() {
        initToolbar()
        initAltitudeGraph()
        initMountainName()
        binding.tracking = args.tracking
    }

    private fun initMountainName() {
        binding.tvHistoryDetailsToolbarTitle.text = args.tracking.mountainName
    }

    private fun initToolbar() {
        with(activity as AppCompatActivity) {
            setSupportActionBar(binding.tbHistoryDetails)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24)
        }
        binding.tbHistoryDetails.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        val uiSettings = naverMap.uiSettings
        with(uiSettings) {
            logoGravity = Gravity.START
            isLogoClickEnabled = true
        }
        if (args.tracking.coordinates.size >= 2) {
            val path = PathOverlay()
            with(path) {
                coords = args.tracking.coordinates.map { LatLng(it.latitude, it.longitude) }
                width = MAP_PATH_WIDTH
                outlineWidth = MAP_PATH_OUTLINE_WIDTH
                color = this@HistoryDetailsFragment.requireContext().getColor(R.color.blue)
                map = naverMap
                val cameraUpdate =
                    CameraUpdate.fitBounds(
                        LatLngBounds.Builder().include(coords).build(),
                        MAP_PADDING
                    )
                        .animate(CameraAnimation.Fly, FLY_DURATION)
                naverMap.moveCamera(cameraUpdate)
            }
        } else if (args.tracking.coordinates.isNotEmpty()) {
            val cameraPosition = CameraPosition(
                LatLng(
                    args.tracking.coordinates[0].latitude,
                    args.tracking.coordinates[0].longitude
                ), 16.0
            )
            naverMap.cameraPosition = cameraPosition
        }
    }

    private fun initAltitudeGraph() {
        if (args.tracking.coordinates.isNotEmpty()) {
            val entries = mutableListOf<Entry>().apply {
                addAll(args.tracking.coordinates.map {
                    BarEntry(
                        LocationService.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS.toFloat()
                                * (args.tracking.coordinates.indexOf(it) + 1) / 1000,
                        String.format("%.2f", it.altitude).toFloat()
                    )
                })
            }

            val set = LineDataSet(entries, "Altitude").apply {
                color = ContextCompat.getColor(requireActivity(), R.color.thick_green)
                lineWidth = 5f
                circleRadius = 1.0f
                setCircleColor(ContextCompat.getColor(requireActivity(), R.color.white))
                setDrawValues(false)
            }

            val dataSet: MutableList<ILineDataSet> = mutableListOf<ILineDataSet>().apply {
                add(set)
            }
            val data = LineData(dataSet)


            with(binding.loBottomSheet) {
                lcAltitudeGraph.run {
                    axisLeft.run {
                        axisMaximum =
                            args.tracking.coordinates.maxOf { it.altitude }.toFloat() + 10f
                        axisMinimum = args.tracking.coordinates.minOf { it.altitude }.toFloat()
                        granularity = 10.0f
                        setDrawLabels(true)
                        setDrawAxisLine(true)
                    }
                    xAxis.run {
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1.0f

                    }
                    axisRight.isEnabled = false
                    animateY(1000)

                    this.data = data
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mvNaver.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mvNaver.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mvNaver.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mvNaver.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mvNaver.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mvNaver.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mvNaver.onLowMemory()
    }

    companion object {
        private const val FLY_DURATION = 2000L
        private const val MAP_PADDING = 50
        private const val MAP_PATH_WIDTH = 10
        private const val MAP_PATH_OUTLINE_WIDTH = 0
    }
}