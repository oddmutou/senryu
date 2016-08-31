package controllers
import play.api._
import play.api.mvc._
import play.api.libs.ws
import play.api.libs.ws.WS
import scala.concurrent._
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global
import twitter4j._
import twitter4j.auth._
import scala.collection.JavaConverters._
import org.atilika.kuromoji.Tokenizer

object Application extends Controller {
  
  def index = Action {
    twitterinfo("test")
    val userName = "Girls_Sense"
    val gsRetweets = getTwitter(userName)
    tweetAnalysis(gsRetweets)
    Ok(views.html.index("test"))
  }



  def getTwitter(userName: String): ResponseList[Status] = {
    val cb = new twitter4j.conf.ConfigurationBuilder
    // GSのAPIキー
    cb.setOAuthConsumerKey("e7yWV3wglOOwZVSH74paJvrfl")//キーは自分で取得してください
      .setOAuthConsumerSecret("oGy5pKh2Ay2jAaouei8wh6p8lDd3hJYYhd0BTa0sb2LZLRO40F")//キーは自分で取得してください
      .setOAuthAccessToken("127681532-4y8sY9ecGBXPwcgZ7pearohHr3EfZwB5mNgiqoHA")//キーは自分で取得してください
      .setOAuthAccessTokenSecret("pd7xLhGdqQOohsflGDirAGRiYlBhNjPo8qrIL1p6hk36S")//キーは自分で取得してください
    
    val twitterFactory = new TwitterFactory(cb.build)
    val tt = twitterFactory.getInstance

    val timeLine = tt.timelines
    timeLine.getRetweetsOfMe
  }

  def tweetAnalysis(gsRetweets: ResponseList[Status]) = {

    val tokenizer = Tokenizer.builder.mode(Tokenizer.Mode.NORMAL).build

    val tokens = for (rl <- gsRetweets) yield tokenizer.tokenize(rl.getText)

    val sorted = tokens.flatten.filter(x => x.getPartOfSpeech.startsWith("名詞")).groupBy(x => x.getBaseForm).values.toList.sortWith(_.length>_.length)

    sorted.foreach { x =>
      x match {
        case x if (x(0).getBaseForm() == null) =>
        case x => println("count: " +x.length+" "+x(0).getBaseForm())
      }
    }

  }








  def twitterinfo(sw : String) = {
    //認証
    val cb = new twitter4j.conf.ConfigurationBuilder
    cb.setOAuthConsumerKey("e7yWV3wglOOwZVSH74paJvrfl")//キーは自分で取得してください
      .setOAuthConsumerSecret("oGy5pKh2Ay2jAaouei8wh6p8lDd3hJYYhd0BTa0sb2LZLRO40F")//キーは自分で取得してください
      .setOAuthAccessToken("127681532-4y8sY9ecGBXPwcgZ7pearohHr3EfZwB5mNgiqoHA")//キーは自分で取得してください
      .setOAuthAccessTokenSecret("pd7xLhGdqQOohsflGDirAGRiYlBhNjPo8qrIL1p6hk36S")//キーは自分で取得してください
    val tf = new TwitterFactory(cb.build)
    val tt = tf.getInstance

    //自分のツイートした内容を表示
    val mts = getTweet(tt, 0, Nil, 1)
    println("mytweet表示件数:" + mts.length + "件")
    for (st <- mts)
      println("@" + st.getUser.getScreenName + "/"
        + st.getUser.getName + ":"
        + st.getText.replace("\n", "")
        + " tweet at " + st.getCreatedAt.formatted("%tY/%<tm/%<td %<tH:%<tM:%<tS")
        + " URL:https://twitter.com/" + st.getUser.getScreenName + "/status/" + st.getId)
/*
    //自分のタイムラインに表示されている内容を表示
    val sts = getTweet(tt, 0, Nil, 2)
    println("mytimeline表示件数:" + sts.length + "件")
    for (st <- sts)
      println("@" + st.getUser.getScreenName + "/"
        + st.getUser.getName + ":"
        + st.getText.replace("\n", "")
        + " tweet at " + st.getCreatedAt.formatted("%tY/%<tm/%<td %<tH:%<tM:%<tS")
        + " URL:https://twitter.com/" + st.getUser.getScreenName + "/status/" + st.getId)

    //検索文字を指定して検索した結果を表示
    val twt = getTweet(tt, 0, Nil, 3, sw)
    println("検索結果:" + twt.length + "件")
    for (st <- twt)
      println("@" + st.getUser.getScreenName + "/"
        + st.getUser.getName + ":"
        + st.getText.replace("\n", "")
        + " tweet at " + st.getCreatedAt.formatted("%tY/%<tm/%<td %<tH:%<tM:%<tS")
        + " URL:https://twitter.com/" + st.getUser.getScreenName + "/status/" + st.getId)
*/
  }

  def getTweet(tt : twitter4j.Twitter, ni : Long, sts : List[twitter4j.Status], pf : Int, sw : String = "", ln : Int = 100) : List[twitter4j.Status] = {
    val pg = new Paging(1, 100)
    val query = new Query
    if (ni != 0) pg.setMaxId(ni)
    try {
      val mts = if (pf == 1) tt.getUserTimeline(pg)
      else if (pf == 2) tt.getHomeTimeline(pg)
      else {
        query.setQuery(sw)
        query.setMaxId(ni)
        val rs = tt.search(query)
        rs.getTweets
      }
      mts.size match {
        case i if i == 0 || sts.length + i > ln => sts
        case _ => getTweet(tt, mts.get(mts.size - 1).getId - 1, sts ::: mts.asScala.toList, pf, sw, ln)
      }
    } catch {
      case e : TwitterException => {
        if (e.getStatusCode == 400 || e.getStatusCode == 429) sts
        else {
            e.printStackTrace
            Nil
        }
      }
      case e : Exception => {
          e.printStackTrace
          Nil
      }
    }
  }

}
