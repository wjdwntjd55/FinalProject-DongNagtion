package likelion.project.dongnation.ui.chatting

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import likelion.project.dongnation.R
import likelion.project.dongnation.databinding.FragmentChattingListBinding
import likelion.project.dongnation.databinding.ItemChattingListRowBinding
import likelion.project.dongnation.databinding.ItemChattingRoomLeavingDialogBinding
import likelion.project.dongnation.model.ChattingRoom
import likelion.project.dongnation.ui.login.LoginViewModel
import likelion.project.dongnation.ui.main.MainActivity

class ChattingListFragment : Fragment() {
    private lateinit var fragmentChattingListBinding: FragmentChattingListBinding
    private lateinit var chattingListViewModel: ChattingListViewModel
    private lateinit var mainActivity: MainActivity

    private lateinit var chattingList: ArrayList<ChattingRoom>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentChattingListBinding = FragmentChattingListBinding.inflate(inflater)
        chattingListViewModel = ViewModelProvider(this)[ChattingListViewModel::class.java]
        mainActivity = activity as MainActivity

        chattingList = ArrayList()

        initUI()
        observe()

        return fragmentChattingListBinding.root
    }

    private fun initUI(){
        fragmentChattingListBinding.run{
            toolbarChattingList.run{
                setTitle(R.string.chatting_list_title)
            }

            recyclerViewChattingList.run{
                adapter = RecyclerAdapter()
                layoutManager = LinearLayoutManager(mainActivity)
                chattingListViewModel.getChattingList()
                chattingListViewModel.notifyNewMessage()
            }
        }
    }

    private fun observe(){
        chattingListViewModel.chattingList.observe(viewLifecycleOwner){
            fragmentChattingListBinding.run{
                recyclerViewChattingList.run{
                    chattingList = chattingListViewModel.chattingList.value!!
                    adapter?.notifyDataSetChanged()
                }
            }
        }
        ChattingListViewModel.receivingState.observe(viewLifecycleOwner){
            chattingListViewModel.getChattingList()
        }
    }

    inner class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){
        inner class ViewHolder(itemChattingListRowBinding: ItemChattingListRowBinding)
            : RecyclerView.ViewHolder(itemChattingListRowBinding.root), OnClickListener, OnLongClickListener {

            var textViewName: TextView
            var textViewMessage: TextView
            var textViewDate: TextView
            var imageViewProfile: ImageView


            init {
                textViewName = itemChattingListRowBinding.textViewItemChattingListName
                textViewMessage = itemChattingListRowBinding.textViewItemChattingListMessage
                textViewDate = itemChattingListRowBinding.textViewItemChattingListDate
                imageViewProfile = itemChattingListRowBinding.imageViewItemChattingListProfile
            }

            // 채팅방 이동
            override fun onClick(p0: View?) {
                val bundle = Bundle()
                bundle.putString("chattingRoomUserIdCounterpart", chattingList[absoluteAdapterPosition].chattingRoomUserIdCounterpart)
                mainActivity.replaceFragment("ChattingFragment", true, bundle)
            }

            // 채팅방 삭제
            override fun onLongClick(p0: View?): Boolean {
                val dialogBuilder = MaterialAlertDialogBuilder(mainActivity)
                val itemChattingRoomLeavingDialogBinding = ItemChattingRoomLeavingDialogBinding.inflate(layoutInflater)
                dialogBuilder.setView(itemChattingRoomLeavingDialogBinding.root)
                val dialog = dialogBuilder.create()

                itemChattingRoomLeavingDialogBinding.apply {
                    buttonChattingRoomLeavingCancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    buttonChattingRoomLeavingLeaving.setOnClickListener {
                        chattingListViewModel.leaveChattingRoom(LoginViewModel.loginUserInfo.userId, chattingList[absoluteAdapterPosition].chattingRoomUserIdCounterpart)
                        dialog.dismiss()
                    }
                }
                dialog.show()

                return true
            }
        }

        // ViewHolder 반환
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemChattingListRowBinding = ItemChattingListRowBinding.inflate(layoutInflater)
            val viewHolder = ViewHolder(itemChattingListRowBinding)

            // 클릭 이벤트 설정
            itemChattingListRowBinding.root.setOnClickListener(viewHolder)
            itemChattingListRowBinding.root.setOnLongClickListener(viewHolder)

            // 가로 세로 길이 설정
            val params = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            itemChattingListRowBinding.root.layoutParams = params

            return viewHolder
        }

        // 전체 행 개수 반환
        override fun getItemCount(): Int {
            return chattingList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textViewName.text = chattingList[position].chattingRoomUserNameCounterpart
            holder.textViewMessage.text = chattingList[position].chattingRoomMessages.last().messageContent
            holder.textViewDate.text = chattingList[position].chattingRoomMessages.last().messageDate
            Glide
                .with(holder.imageViewProfile)
                .load(chattingList[position].chattingRoomUserProfileCounterpart.toUri())
                .circleCrop()
                .into(holder.imageViewProfile)
        }
    }
}