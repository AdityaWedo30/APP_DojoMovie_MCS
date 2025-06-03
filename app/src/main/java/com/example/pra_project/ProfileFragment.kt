package com.example.pra_project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var tvUsername: TextView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi views
        tvUsername = view.findViewById(R.id.tv_username)
        logoutButton = view.findViewById(R.id.btn_logout)

        // Tampilkan nomor HP user yang login
        displayUserPhone()

        // Handle logout
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun displayUserPhone() {
        try {
            val sharedPref = activity?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val loggedInPhone = sharedPref?.getString("loggedInPhone", "")

            if (!loggedInPhone.isNullOrEmpty()) {
                tvUsername.text = loggedInPhone
            } else {
                tvUsername.text = "User tidak ditemukan"
            }
        } catch (e: Exception) {
            tvUsername.text = "Error loading user"
        }
    }

    private fun logout() {
        try {
            activity?.let { context ->
                logout(context)
            }
        } catch (e: Exception) {
            // Handle error jika ada masalah saat logout
        }
    }

    // Fungsi logout global
    fun logout(context: Context) {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        // Redirect ke LoginActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}