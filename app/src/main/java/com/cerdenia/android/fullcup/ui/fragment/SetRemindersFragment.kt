package com.cerdenia.android.fullcup.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cerdenia.android.fullcup.data.model.Reminder
import com.cerdenia.android.fullcup.databinding.FragmentSetRemindersBinding
import com.cerdenia.android.fullcup.ui.adapter.ReminderAdapter
import com.cerdenia.android.fullcup.ui.dialog.SetReminderFragment
import com.cerdenia.android.fullcup.ui.viewmodel.SetRemindersViewModel

class SetRemindersFragment : Fragment(), ReminderAdapter.Listener {
    private var _binding: FragmentSetRemindersBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SetRemindersViewModel
    private lateinit var adapter: ReminderAdapter
    private var callbacks: Callbacks? = null

    interface Callbacks {
        fun onRemindersConfirmed()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SetRemindersViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ReminderAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.setRemindersButton.setOnClickListener {
            // TODO: Save reminders to Google Calendar then change screens
            callbacks?.onRemindersConfirmed()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.remindersLive.observe(viewLifecycleOwner, { reminders ->
            adapter.submitList(reminders)
            adapter.notifyDataSetChanged()
            // Enable Set Reminders button if all reminders are ready.
            val isReady = !reminders.any { !it.isSet }
            binding.setRemindersButton.isEnabled = isReady
        })

        parentFragmentManager.setFragmentResultListener(
            SetReminderFragment.KEY_CONFIRM,
            viewLifecycleOwner,
            { _, result ->
                val reminder = result.getSerializable(SetReminderFragment.ARG_REMINDER) as Reminder
                Log.i(TAG, "Got reminder: $reminder")
                viewModel.updateReminder(reminder)
            }
        )
    }

    override fun onItemSelected(reminder: Reminder) {
        SetReminderFragment
            .newInstance(reminder, viewModel.getFreeTimes())
            .show(parentFragmentManager, SetReminderFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    companion object {
        private const val TAG = "SetRemindersFragment"

        fun newInstance(): SetRemindersFragment {
            return SetRemindersFragment()
        }
    }
}