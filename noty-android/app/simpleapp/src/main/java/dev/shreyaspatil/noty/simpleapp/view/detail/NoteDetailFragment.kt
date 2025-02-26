/*
 * Copyright 2020 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shreyaspatil.noty.simpleapp.view.detail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.shreyaspatil.noty.core.ui.UIDataState
import dev.shreyaspatil.noty.simpleapp.R
import dev.shreyaspatil.noty.simpleapp.databinding.NoteDetailFragmentBinding
import dev.shreyaspatil.noty.simpleapp.view.base.BaseFragment
import dev.shreyaspatil.noty.utils.saveBitmap
import dev.shreyaspatil.noty.utils.share.shareImage
import dev.shreyaspatil.noty.utils.share.shareNoteText
import dev.shreyaspatil.noty.utils.validator.NoteValidator
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NoteDetailFragment : BaseFragment<NoteDetailFragmentBinding, NoteDetailViewModel>() {

    private val args: NoteDetailFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelAssistedFactory: NoteDetailViewModel.Factory

    override val viewModel: NoteDetailViewModel by viewModels {
        args.noteId?.let { noteId ->
            NoteDetailViewModel.provideFactory(viewModelAssistedFactory, noteId)
        } ?: throw IllegalStateException("'noteId' shouldn't be null")
    }

    private val requestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) shareImage() else showErrorDialog(
            title = getString(R.string.dialog_title_failed_image_share),
            message = getString(R.string.dialog_message_failed_image_share)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        observeNote()
        observeNoteUpdate()
        observeNoteDeletion()
    }

    private fun initViews() {
        binding.run {
            fabSave.setOnClickListener { onNoteSaveClicked() }
            noteLayout.fieldTitle.addTextChangedListener { onNoteContentChanged() }
            noteLayout.fieldNote.addTextChangedListener { onNoteContentChanged() }
        }
    }

    private fun onNoteSaveClicked() {
        val (title, note) = getNoteContent()
        viewModel.updateNote(title, note)
    }

    private fun observeNote() {
        viewModel.note.asLiveData().observe(viewLifecycleOwner) {
            binding.run {
                binding.noteLayout.fieldTitle.setText(it.title)
                binding.noteLayout.fieldNote.setText(it.note)
                fabSave.isEnabled = true
            }
        }
    }

    private fun observeNoteUpdate() {
        viewModel.updateNoteState.asLiveData().observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is UIDataState.Loading -> showProgressDialog()
                is UIDataState.Success -> {
                    hideProgressDialog()
                    findNavController().navigateUp()
                }
                is UIDataState.Failed -> {
                    hideProgressDialog()
                    toast("Error: ${viewState.message}")
                }
            }
        }
    }

    private fun observeNoteDeletion() {
        viewModel.deleteNoteState.asLiveData().observe(viewLifecycleOwner) { viewState ->
            when (viewState) {
                is UIDataState.Loading -> showProgressDialog()
                is UIDataState.Success -> {
                    hideProgressDialog()
                    findNavController().navigateUp()
                }
                is UIDataState.Failed -> hideProgressDialog()
            }
        }
    }

    private fun onNoteContentChanged() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val previousNote = viewModel.note.first()

            val (newTitle, newNote) = getNoteContent()

            if ((
                previousNote.title != newTitle.trim() ||
                    previousNote.note.trim() != newNote.trim()
                ) &&
                NoteValidator.isValidNote(newTitle, newNote)
            ) {
                binding.fabSave.show()
            } else binding.fabSave.hide()
        }
    }

    private fun getNoteContent() = binding.noteLayout.let {
        Pair(
            it.fieldTitle.text.toString(),
            it.fieldNote.text.toString()
        )
    }

    private fun shareText() {
        val title = binding.noteLayout.fieldTitle.text.toString()
        val note = binding.noteLayout.fieldNote.text.toString()

        requireContext().shareNoteText(title, note)
    }

    private fun shareImage() {
        if (!isStoragePermissionGranted()) {
            requestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        val imageUri = binding.noteLayout.noteContentLayout.drawToBitmap().let { bitmap ->
            saveBitmap(requireActivity(), bitmap)
        } ?: run {
            toast("Error occurred!")
            return
        }

        requireContext().shareImage(imageUri)
    }

    private fun isStoragePermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> viewModel.deleteNote()
            R.id.action_share_text -> shareText()
            R.id.action_share_image -> shareImage()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = NoteDetailFragmentBinding.inflate(inflater, container, false)
}
