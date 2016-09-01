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
import org.atilika.kuromoji.Token

object Application extends Controller {
  
  def index = Action {
    val getTwitter : Option[Twitter] = Cache.getAs[Twitter]("twitter_obj")
    getTwitter match {
      case Some(twitter) => {
        val results : List[String] = generateSongs(twitter, 5)
        Ok(views.html.index(results))
      }
      case _ => {
        Ok(views.html.login("Login"))
      }
    }
  }

  def generateSongs(twitter : Twitter, count : Int) : List[String] = {
    val dictionary:List[(String,Int)] = getDictionary(twitter)
    val fiveCharacters : List[String] = dictionary.filter{
      u => u match {case (x, y) => y == 5 case _ => false}
    }.map{
      u => u match {case (x, y) => x case _ => ""}
    }
    val sevenCharacters : List[String] = dictionary.filter{
      u => u match {case (x, y) => y == 7 case _ => false}
    }.map{
      u => u match {case (x, y) => x case _ => ""}
    }
    List(generateSong(fiveCharacters, sevenCharacters))
  }

  def generateSong(fiveCharacters : List[String], sevenCharacters : List[String]) : String = {
    scala.util.Random.shuffle(fiveCharacters) 
    scala.util.Random.shuffle(sevenCharacters)
    fiveCharacters.last + sevenCharacters.last + fiveCharacters.head
  }

  def getDictionary(twitter : Twitter) : List[(String, Int)] = {
    tweetsParse(twitter.timelines.getUserTimeline(twitter.getId, new Paging(1, 500)))
  }

  def tweetsParse(tweets : ResponseList[twitter4j.Status]) : List[(String, Int)]= {
    val buff = for (tweet <- tweets) yield { tweetAnalysis(tweet.getText) }
    buff.toList.flatMap(t => t)
  }

  def tweetAnalysis(tweet : String): List[(String, Int)] = {
      val tokenizer = Tokenizer.builder.mode(Tokenizer.Mode.NORMAL).build
      getSyllableList(tokenizer.tokenize(tweet).toArray)
  }

  def getSyllableList(tokens :  Array[Object]) : List[(String, Int)] ={
    tokens.flatMap(t => getSyllable(t.asInstanceOf[Token])).toList
  }

  def getSyllable(token : Token) : List[(String, Int)] = {
    val str : String = token.getReading
    val len = {
      str match {
        case null => 0
        case x => x.length
      }
    }
    List((token.getSurfaceForm, len))
  }



  
















}
