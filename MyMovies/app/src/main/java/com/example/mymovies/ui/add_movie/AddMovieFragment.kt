package com.example.mymovies.ui.add_movie

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymovies.R
import com.example.mymovies.databinding.AddMovieFragmentBinding
import com.example.mymovies.utils.Constants
import com.example.mymovies.utils.Error
import com.example.mymovies.utils.Loading
import com.example.mymovies.utils.Success
import com.example.mymovies.utils.autoCleared
import com.example.mymovies.utils.eSearchBy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddMovieFragment : Fragment(), MovieSearchItemsAdapter.MovieSearchItemListener{

    private val viewModel : AddMoviesViewModel by viewModels()

    private var binding : AddMovieFragmentBinding by autoCleared()

    private lateinit var adapter: MovieSearchItemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AddMovieFragmentBinding.inflate(inflater,container,false)
        binding.searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().length > 2 || s.toString().length == 0){
                    setNewMoviesList(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.searchByRadio.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.title_radio -> viewModel.searchByParam = eSearchBy.Title
                R.id.genre_radio -> viewModel.searchByParam = eSearchBy.Genre
            }
            setNewMoviesList(binding.searchInput.text.toString())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MovieSearchItemsAdapter(this, requireContext())
        binding.recycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.recycleView.adapter = adapter

        viewModel.genresDict.observe(viewLifecycleOwner){
            when(it.status){
                is Loading -> showProgressBar()
                is Success -> {
                    Constants.setGenresDict(it.status.data!!.getDict())
                    viewModel.fetchedGenres = true
                    setNewMoviesList("")
                }
                is Error -> {
                    showProgressBar()
                    Toast.makeText(requireContext(), requireContext().getString(R.string.no_connection), Toast.LENGTH_SHORT)
                }
            }
        }

        viewModel.moviesList.observe(viewLifecycleOwner){
            if(it != null){
                when(it.status){
                    is Loading -> {
                        showProgressBar()
                    }
                    is Success -> {
                        if(viewModel.fetchedGenres) {
                            adapter.setMovies(it.status.data!!)
                            showItems()
                            if (it.status.data.moviesList.size == 0) {
                                showNoResults()
                            }
                        }
                    }
                    is Error -> {
                        println("error - ${it.status.message}")
                    }
                }
            }
        }
    }

    override fun OnMovieClick(movieId: Int) {
        viewModel.addItem(movieId).observe(viewLifecycleOwner) {
            when (it.status) {
                is Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Success -> {
                    if (it.status.data != null) {
                        findNavController().navigate(R.id.action_addMovieFragment_to_allMoviesFragment)
                    }
                }

                is Error -> {
                    Log.d("Error", "Inside Error")
                    showItems()
                    raiseNoConnectionToast()
                }
            }
        }
    }

    private fun raiseNoConnectionToast(){
        Toast.makeText(context, "No Connection", Toast.LENGTH_SHORT).show()
    }
    private fun setNewMoviesList(query : String) {
        var mutableQuery = query
        var validQuery = true

        if(binding.genreRadio.isChecked){
            if(Constants.STRING_TO_ID_GENRES.containsKey(mutableQuery)){
                mutableQuery = Constants.STRING_TO_ID_GENRES[mutableQuery].toString()
            }
            else{
                showNoResults()
                validQuery = false
            }
        }

        if(validQuery){
            showItems()
            viewModel.setKeyword(mutableQuery)
        }
    }

    private fun showNoResults(){
        binding.recycleView.visibility = View.GONE
        binding.noResultsText.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun showItems(){
        binding.recycleView.visibility = View.VISIBLE
        binding.noResultsText.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showProgressBar(){
        binding.recycleView.visibility = View.GONE
        binding.noResultsText.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }
}