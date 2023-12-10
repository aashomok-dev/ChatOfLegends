package com.ashomok.heroai.activities.main.messaging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ashomok.heroai.model.realms.Message

class ChatViewModel : ViewModel() {

    private val _itemSelectedLiveData = MutableLiveData<List<Message>>()
    val itemSelectedLiveData: LiveData<List<Message>> = _itemSelectedLiveData

    private val _selectedItems = arrayListOf<Message>()
    val selectedItems: List<Message> = _selectedItems

    fun itemSelected(message: Message) {
        if (_selectedItems.contains(message))
            _selectedItems.remove(message)
        else {
            _selectedItems.add(message)
        }

        _itemSelectedLiveData.value = _selectedItems
    }

    fun clearSelectedItems() {
        _selectedItems.clear()
    }
}