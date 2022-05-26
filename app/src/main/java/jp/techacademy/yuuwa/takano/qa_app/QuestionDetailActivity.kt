package jp.techacademy.yuuwa.takano.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_setting.*

class QuestionDetailActivity : AppCompatActivity() {
    private var fGen = 0
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {



        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {


        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    private val mfavoriteListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val questionUid = dataSnapshot.key ?: ""

                if (questionUid != mQuestion.questionUid) {
                    return
                }

                fav.visibility = View.VISIBLE
                no_fav.visibility = View.INVISIBLE

        }



        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {


        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser//ログインユーザー情報
        if(user == null) {
            fav.visibility = View.INVISIBLE
            no_fav.visibility = View.INVISIBLE
        }else {
            fav.visibility = View.INVISIBLE
            no_fav.visibility = View.VISIBLE
            val database = FirebaseDatabase.getInstance()
            val favoriteref = database.getReference(FavoritePATH)
            favoriteref.child(user!!.uid).addChildEventListener(mfavoriteListener)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question
        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()



        fab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser//ログインユーザー情報
            // ログイン済みのユーザーを取得する
            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }



        fav.setOnClickListener {//お気に入り削除
            val database = FirebaseDatabase.getInstance()
            val user = FirebaseAuth.getInstance().currentUser//ログインユーザー情報
            val favoriteref = database.getReference(FavoritePATH)
            favoriteref.child(user!!.uid).child(mQuestion.questionUid).setValue(null)
            fav.visibility = View.INVISIBLE
            no_fav.visibility = View.VISIBLE
        }

        no_fav.setOnClickListener {//お気に入り登録
            val database = FirebaseDatabase.getInstance()
            val user = FirebaseAuth.getInstance().currentUser//ログインユーザー情報
            val favoriteref = database.getReference(FavoritePATH)
            favoriteref.child(user!!.uid).child(mQuestion.questionUid).child("genre").setValue(mQuestion.genre.toString())
            fav.visibility = View.VISIBLE
            no_fav.visibility = View.INVISIBLE
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

    }
}