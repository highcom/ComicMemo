package com.highcom.comicmemo.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.highcom.comicmemo.databinding.FragmentAuthorEditBinding
import com.highcom.comicmemo.viewmodel.AuthorEditViewModel

/**
 * 著作者名の一覧表示と編集用のFragment
 *
 */
class AuthorEditFragment : Fragment() {
    private lateinit var binding:FragmentAuthorEditBinding
    private lateinit var authorEditAdapter: AuthorEditAdapter
    /** Activityで生成されたViewModelを利用する */
    private val viewModel: AuthorEditViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorEditBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authorEditAdapter = AuthorEditAdapter()
        binding.authorEditView.adapter = authorEditAdapter
        viewModel.authors.observe(viewLifecycleOwner) {
            authorEditAdapter.submitList(it)
        }
    }
}