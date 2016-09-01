package controllers
import play.api._
import play.api.cache._
import play.api.mvc._
import play.api.libs.ws
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent._
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global
import twitter4j._
import twitter4j.auth._
import twitter4j.conf._
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import org.atilika.kuromoji.Tokenizer

object Application extends Controller {
  
  def index = Action {
    val getTwitter : Option[Twitter] = Cache.getAs[Twitter]("twitter_obj")
    getTwitter match {
      case Some(twitter) => {
        val gsRetweets = getTimeline(twitter)
        tweetAnalysis(gsRetweets)
        Ok(views.html.index("success!"))
      }
      case _ => {
        Ok(views.html.login("Login"))
      }
    }
    

  }



  def getTimeline(getTwitter : Twitter): ResponseList[twitter4j.Status] = {
    val cb = new twitter4j.conf.ConfigurationBuilder
    // GSのAPIキー
    cb.setOAuthConsumerKey("e7yWV3wglOOwZVSH74paJvrfl")//キーは自分で取得してください
      .setOAuthConsumerSecret("oGy5pKh2Ay2jAaouei8wh6p8lDd3hJYYhd0BTa0sb2LZLRO40F")//キーは自分で取得してください
      .setOAuthAccessToken("127681532-4y8sY9ecGBXPwcgZ7pearohHr3EfZwB5mNgiqoHA")//キーは自分で取得してください
      .setOAuthAccessTokenSecret("pd7xLhGdqQOohsflGDirAGRiYlBhNjPo8qrIL1p6hk36S")//キーは自分で取得してください
    
    val twitterFactory = new TwitterFactory(cb.build)
    val tt = getTwitter

    val timeLine = tt.timelines
    timeLine.getUserTimeline
  }

  def tweetAnalysis(gsRetweets: ResponseList[twitter4j.Status]) = {

    val tokenizer = Tokenizer.builder.mode(Tokenizer.Mode.NORMAL).build
    
    val tokens = for (rl <- gsRetweets) yield {
        println(rl.getText)
        tokenizer.tokenize(rl.getText)
    }

    val sorted = tokens.flatten.filter(x => x.getPartOfSpeech.startsWith("名詞")).groupBy(x => x.getBaseForm).values.toList.sortWith(_.length>_.length)

    sorted.foreach { x =>
      println(x)
      x match {
        case x if (x(0).getBaseForm() == null) =>
        case x => println("count: " +x.length+" "+x(0).getBaseForm())
      }
    }

  }





  
















}
