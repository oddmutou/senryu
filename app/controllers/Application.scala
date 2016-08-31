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
    // GS$B$N(BAPI$B%-!<(B
    cb.setOAuthConsumerKey("e7yWV3wglOOwZVSH74paJvrfl")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
      .setOAuthConsumerSecret("oGy5pKh2Ay2jAaouei8wh6p8lDd3hJYYhd0BTa0sb2LZLRO40F")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
      .setOAuthAccessToken("127681532-4y8sY9ecGBXPwcgZ7pearohHr3EfZwB5mNgiqoHA")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
      .setOAuthAccessTokenSecret("pd7xLhGdqQOohsflGDirAGRiYlBhNjPo8qrIL1p6hk36S")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
    
    val twitterFactory = new TwitterFactory(cb.build)
    val tt = twitterFactory.getInstance

    val timeLine = tt.timelines
    timeLine.getRetweetsOfMe
  }

  def tweetAnalysis(gsRetweets: ResponseList[Status]) = {

    val tokenizer = Tokenizer.builder.mode(Tokenizer.Mode.NORMAL).build

    val tokens = for (rl <- gsRetweets) yield tokenizer.tokenize(rl.getText)

    val sorted = tokens.flatten.filter(x => x.getPartOfSpeech.startsWith("$BL>;l(B")).groupBy(x => x.getBaseForm).values.toList.sortWith(_.length>_.length)

    sorted.foreach { x =>
      x match {
        case x if (x(0).getBaseForm() == null) =>
        case x => println("count: " +x.length+" "+x(0).getBaseForm())
      }
    }

  }








  def twitterinfo(sw : String) = {
    //$BG'>Z(B
    val cb = new twitter4j.conf.ConfigurationBuilder
    cb.setOAuthConsumerKey("e7yWV3wglOOwZVSH74paJvrfl")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
      .setOAuthConsumerSecret("oGy5pKh2Ay2jAaouei8wh6p8lDd3hJYYhd0BTa0sb2LZLRO40F")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
      .setOAuthAccessToken("127681532-4y8sY9ecGBXPwcgZ7pearohHr3EfZwB5mNgiqoHA")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
      .setOAuthAccessTokenSecret("pd7xLhGdqQOohsflGDirAGRiYlBhNjPo8qrIL1p6hk36S")//$B%-!<$O<+J,$G<hF@$7$F$/$@$5$$(B
    val tf = new TwitterFactory(cb.build)
    val tt = tf.getInstance

    //$B<+J,$N%D%$!<%H$7$?FbMF$rI=<((B
    val mts = getTweet(tt, 0, Nil, 1)
    println("mytweet$BI=<(7o?t(B:" + mts.length + "$B7o(B")
    for (st <- mts)
      println("@" + st.getUser.getScreenName + "/"
        + st.getUser.getName + ":"
        + st.getText.replace("\n", "")
        + " tweet at " + st.getCreatedAt.formatted("%tY/%<tm/%<td %<tH:%<tM:%<tS")
        + " URL:https://twitter.com/" + st.getUser.getScreenName + "/status/" + st.getId)
/*
    //$B<+J,$N%?%$%`%i%$%s$KI=<($5$l$F$$$kFbMF$rI=<((B
    val sts = getTweet(tt, 0, Nil, 2)
    println("mytimeline$BI=<(7o?t(B:" + sts.length + "$B7o(B")
    for (st <- sts)
      println("@" + st.getUser.getScreenName + "/"
        + st.getUser.getName + ":"
        + st.getText.replace("\n", "")
        + " tweet at " + st.getCreatedAt.formatted("%tY/%<tm/%<td %<tH:%<tM:%<tS")
        + " URL:https://twitter.com/" + st.getUser.getScreenName + "/status/" + st.getId)

    //$B8!:wJ8;z$r;XDj$7$F8!:w$7$?7k2L$rI=<((B
    val twt = getTweet(tt, 0, Nil, 3, sw)
    println("$B8!:w7k2L(B:" + twt.length + "$B7o(B")
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
