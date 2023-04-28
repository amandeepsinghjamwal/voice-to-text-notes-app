package com.example.notesapp

import android.app.Activity
import android.content.ClipData.Item
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.NotesLayoutBinding

class AdapterClass(val activity: Activity,private val onItemClicked: (NotesData) -> Unit) : ListAdapter<NotesData,AdapterClass.ItemViewHolder>(DiffCallBack) {
    inner class ItemViewHolder(var binding:NotesLayoutBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NotesData){
            binding.apply {
                binding.noteText.text= data.value
            }
        }
    }
    companion object{
        private val DiffCallBack= object : DiffUtil.ItemCallback<NotesData>(){
            override fun areItemsTheSame(oldItem: NotesData, newItem: NotesData): Boolean {
                return oldItem.key==newItem.key
            }

            override fun areContentsTheSame(oldItem: NotesData, newItem: NotesData): Boolean {
                return oldItem==newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(NotesLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current=getItem(position)
        holder.itemView.setOnClickListener{
            onItemClicked(current)
        }
        holder.bind(current)
    }
}