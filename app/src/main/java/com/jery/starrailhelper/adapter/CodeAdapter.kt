package com.jery.starrailhelper.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jery.starrailhelper.R
import com.jery.starrailhelper.data.CodeItem
import com.jery.starrailhelper.databinding.BottomSheetLayoutBinding
import com.jery.starrailhelper.databinding.ItemCodeBinding
import com.jery.starrailhelper.databinding.ItemHeaderBinding
import com.jery.starrailhelper.databinding.ItemRewardBinding
import com.jery.starrailhelper.utils.Utils

class CodeAdapter(
    private val activeCodes: List<CodeItem>,
    private val expiredCodes: List<CodeItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CODE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_CODE -> {
                val binding = ItemCodeBinding.inflate(inflater, parent, false)
                CodeViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                if (position == 0) {
                    holder.bind("Active Codes")
                } else {
                    holder.bind("Expired Codes")
                }
            }
            is CodeViewHolder -> {
                val codeItem = getCodeItem(position)
                holder.bind(codeItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return activeCodes.size + expiredCodes.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0, activeCodes.size + 1 -> VIEW_TYPE_HEADER
            else -> VIEW_TYPE_CODE
        }
    }

    private fun getCodeItem(position: Int): CodeItem {
        return if (position < activeCodes.size + 1) {
            activeCodes[position - 1]
        } else {
            expiredCodes[position - activeCodes.size - 2]
        }
    }

    inner class HeaderViewHolder(private val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: String) {
            binding.tvHeader.text = header
        }
    }

    inner class CodeViewHolder(private val binding: ItemCodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(codeItem: CodeItem) {
            val ctx = itemView.context
            with (binding) {
                this.codeItem = codeItem
                this.utils = Utils
                executePendingBindings()
                itemView.setOnClickListener { onItemClick(codeItem, ctx) }
                ivIcon.setOnClickListener { codeItem.isRedeemed = (!codeItem.isRedeemed); notifyItemChanged(layoutPosition) }
                ivRedeem.setOnClickListener { redeem(codeItem.code, ctx); codeItem.isRedeemed = true; notifyItemChanged(layoutPosition) }
            }
            try {
                codeItem.rewards.first().imageURL!!
                Glide.with(ctx)
                    .load(codeItem.rewards.first().imageURL!!)
                    .placeholder(R.drawable.ic_timer)
                    .fitCenter()
                    .transition(withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .into(binding.ivIcon)
            } catch (e: Exception) {
//                Utils.showStackTrace(ctx, e)
//                e.printStackTrace()
            }
        }

        @SuppressLint("SetTextI18n")
        private fun onItemClick(codeItem: CodeItem, ctx: Context) {
            val bottomSheetDialog = BottomSheetDialog(ctx)
            val dialogBinding = BottomSheetLayoutBinding.inflate(LayoutInflater.from(ctx), null, false)
            bottomSheetDialog.setContentView(dialogBinding.root)

            dialogBinding.rewards.visibility = View.VISIBLE
            val rewardsLayout = dialogBinding.rewardsLayout

            rewardsLayout.removeAllViews()
            for (reward in codeItem.rewards) {
                val rewardBinding = ItemRewardBinding.inflate(LayoutInflater.from(ctx), rewardsLayout, false)
                rewardsLayout.addView(rewardBinding.root)

                rewardBinding.rewardQuantity.text = "x" + reward.quantity.toString()
                try {
                    Glide.with(ctx)
                        .load(reward.imageURL)
                        .placeholder(R.drawable.ic_timer)
                        .fitCenter()
                        .transition( withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()) )
                        .into(rewardBinding.rewardImage)
                } catch (e: Exception) {
//                    e.printStackTrace()
                    e.printStackTrace()
                }
            }

            dialogBinding.rCode.text = codeItem.code
            dialogBinding.rCode.setOnClickListener { Utils.copyToClipboard(codeItem.code) }
            dialogBinding.rStatus.text = codeItem.duration.first +" / "+ codeItem.duration.second
            dialogBinding.redeemBtn.text = if (codeItem.isRedeemed) "Redeem once again" else "Redeem Code"
            dialogBinding.redeemBtn.setOnClickListener { redeem(codeItem.code, ctx); codeItem.isRedeemed = true; notifyItemChanged(layoutPosition) }
            bottomSheetDialog.show()
        }

        private fun redeem(code: String, ctx: Context) {
            Utils.copyToClipboard(code)

            val bottomSheetDialog = BottomSheetDialog(ctx)
            val dialogView = LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_layout, null)
            bottomSheetDialog.setContentView(dialogView)
            val progress = dialogView.findViewById<ProgressBar>(R.id.progress)

            val wv = dialogView.findViewById<WebView>(R.id.webView)
            wv.visibility = View.VISIBLE
            wv.apply {
                background = ContextCompat.getDrawable(ctx, R.color.colorAccent)
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        progress.visibility = View.VISIBLE
                    }
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        wv.evaluateJavascript(script(code), null)
                        progress.visibility = View.GONE
                    }
                }
            }
            wv.loadUrl("https://hsr.hoyoverse.com/gift?code=$code")
            bottomSheetDialog.show()
        }
        private fun script(code: String) = """
            function pollStart() {
                const element = document.querySelector("span.mihoyo-account-role__nickname");
                if (document.querySelector("span.mihoyo-account-role__nickname")) {
                    const server = document.querySelector("div.web-cdkey-form__select--toggle");
                    server.click();
                    function pollServer() {
                        if (server.textContent.trim() != "Select a server") {
                            const codeInput = document.querySelector("input#web_cdkey_code");
                            function pollCode() {
                                if (!codeInput.disabled) codeInput.value = "$code";
                                else setTimeout(pollCode, 500);
                            }
                            pollCode();
                        } else setTimeout(pollServer, 500)
                    }
                    server.addEventListener("click", pollServer);
                    pollServer();
                } else setTimeout(pollStart, 500);
            }
            pollStart();
        """.trimIndent()
    }
}
