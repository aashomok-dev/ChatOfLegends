package com.ashomok.heroai.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ashomok.heroai.utils.RealmHelper
//import com.ashomok.ai_chat.utils.DownloadManager.Companion.cancelAllTasks
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.SupervisorJob

/**
 * Created by Devlomi on 31/12/2017.
 */
//this is responsible for sending and receiving files/data from firebase using Download Manager Class
class NetworkService : Service() {
    private val disposables = CompositeDisposable()
    private val parentJob = SupervisorJob()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null) {
            val chatId = intent.getStringExtra(com.ashomok.heroai.utils.IntentUtils.EXTRA_CHAT_ID)
            val messageId =
                intent.getStringExtra(com.ashomok.heroai.utils.IntentUtils.EXTRA_MESSAGE_ID)
            if (intent.action == com.ashomok.heroai.utils.IntentUtils.INTENT_ACTION_UPDATE_MESSAGE_STATE) {
                val state =
                    intent.getIntExtra(com.ashomok.heroai.utils.IntentUtils.EXTRA_STAT, 0)
                updateMessageStat2(messageId, state)
            }
        }
        return START_STICKY
    }

    private fun updateMessageStat2(messageId: String?, state: Int) {
        disposables.add(updateMessagesState(messageId!!, state).subscribe())
    }

    private fun updateMessageStat(messageId: String, stat: Int): Completable {
        return Completable.fromAction {
            RealmHelper.getInstance().updateMessageStatLocally(messageId, stat)
            RealmHelper.getInstance().deleteUnUpdateStat(messageId)
        }
    }

    private fun updateMessagesState(
        messageId: String,
        state: Int
    ): Completable {
        return updateMessageStat(messageId, state)
            .andThen(Observable.fromIterable(RealmHelper.getInstance().unUpdateMessageStat))
            .flatMapCompletable { unUpdatedStat ->
                return@flatMapCompletable updateMessageStat(
                    unUpdatedStat.messageId,
                    unUpdatedStat.statToBeUpdated
                ).andThen {
                    RealmHelper.getInstance().updateMessageStatLocally(
                        unUpdatedStat.messageId,
                        unUpdatedStat.statToBeUpdated
                    )
                    RealmHelper.getInstance()
                        .deleteUnUpdateStat(unUpdatedStat.messageId)

                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
        cancelCoroutineJob()
    }

    private fun cancelCoroutineJob() = try {
        parentJob.cancel()
    } catch (e: Exception) {
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}