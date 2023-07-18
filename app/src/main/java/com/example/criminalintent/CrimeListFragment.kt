package com.example.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.example.criminalintent.model.Crime
import com.example.criminalintent.model.CrimeListViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
        //fun recyclerViewIsEmpty(isEmpty: Boolean)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter()


    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)

        val searchItem = menu.findItem(R.id.search_crime)
        val searchView = (searchItem.actionView as SearchView).apply {
            queryHint = "Type crime title"

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter?.filter?.filter(newText)
                    return false
                }
            })

            setOnCloseListener {
                adapter?.submitList(crimeListViewModel.crimeListLiveData.value)
                false
            }

        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()

                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)

                true
            }

            R.id.sort_crimes -> {
                adapter?.submitList(adapter?.currentList?.sortedBy {it.title})
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /*override fun onResume() {
        super.onResume()

        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner) {
            it?.let {
                callbacks?.recyclerViewIsEmpty(it.isEmpty())
            }
        }
    }*/

    /*override fun onStop() {
        super.onStop()
        callbacks?.recyclerViewIsEmpty(false)
    }*/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner) {
            it?.let {
                Log.i(TAG, "Got crimes ${it.size}")
                adapter?.submitList(it.toMutableList())

                //callbacks?.recyclerViewIsEmpty(it.isEmpty())
            }
        }

        super.onViewCreated(view, savedInstanceState)

    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime

            titleTextView.text = this.crime.title
            dateTextView.text = SimpleDateFormat("yyyy/MMM/dd").format(this.crime.date)

            solvedImageView.visibility =
                if (crime.isSolved) View.VISIBLE
             else View.GONE
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter : ListAdapter<Crime, CrimeHolder>(CrimeComparator()), Filterable {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val layout = R.layout.list_item_crime
            val view = layoutInflater.inflate(layout, parent, false)
            return CrimeHolder(view)
        }

        override fun getItemCount() = currentList.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = currentList[position]
            holder.bind(crime)
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val filteredList = mutableListOf<Crime>()

                    if (constraint == null || constraint.isEmpty())
                        crimeListViewModel.crimeListLiveData.value?.let {filteredList.addAll(it)}

                    else {
                        crimeListViewModel.crimeListLiveData.value?.let { crimeList ->
                            for (crime in crimeList) {
                                if (crime.title.lowercase().contains(constraint.toString().lowercase()))
                                    filteredList.add(crime)
                            }
                        }
                    }

                    return FilterResults().apply { values = filteredList }
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    if (results != null) {
                        adapter?.submitList(results.values as List<Crime>)
                    }
                }

            }
        }
    }

    companion object {
        const val ARG_CRIME_ID = "crime_id"
    }

    private inner class CrimeComparator : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.isSolved == newItem.isSolved &&
                    oldItem.date == newItem.date &&
                    oldItem.title == newItem.title
        }
    }
}
