package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

import play.api.cache._

import twitter4j._
import twitter4j.auth._

/**
 * ツイッターログインコントローラ
 */
object TwitterController extends Controller {

  /**
   * Twitterへログインする
   */
  def twitterLogin = Action { implicit request =>
    val consumerKey = "e7yWV3wglOOwZVSH74paJvrfl"
    val consumerSecret = "oGy5pKh2Ay2jAaouei8wh6p8lDd3hJYYhd0BTa0sb2LZLRO40F"
    // Twitterオブジェクトの初期化
    val twitter: Twitter = (new TwitterFactory()).getInstance()
    // RequestTokenの取得
    twitter.setOAuthConsumer(consumerKey, consumerSecret)
    val requestToken: RequestToken = twitter.getOAuthRequestToken("http://vps2.o625.com/twitterOAuthCallback")

    // TwitterとRequestTokenのオブジェクトをCacheに格納(2分有効)
    Cache.set("twitter", twitter, 120)
    Cache.set("requestToken", requestToken, 120)

    // Twitterのログイン画面にリダイレクト
    Redirect(requestToken.getAuthorizationURL())
  }


  /**
   * TwitterからのCallBack処理
   */
  def twitterOAuthCallback = Action { implicit request =>
    // 承認可否を精査（deniedがあったら承認キャンセル）
    request.queryString.get("denied") match {

      // Twitterのアプリケーション承認キャンセル時
      case Some(denied) => Redirect(routes.TwitterController.twitterLogout)

      // Twitterのアプリケーション承認時
      case _ => {

        // TwitterのオブジェクトをCacheから取得
        val getTwitter     : Option[Twitter] = Cache.getAs[Twitter]("twitter")

        // 取得できない場合はトップ画面へ
        getTwitter match {
          case Some(twitter) => {

            // RequestTokenのオブジェクトをCacheから取得
            val getRequestToken: Option[RequestToken] = Cache.getAs[RequestToken]("requestToken")

            // 取得できない場合はトップ画面へ
            getRequestToken match {
              case Some(requestToken) => {

                // AuthTokenを取得する
                var authToken   : String = request.queryString.get("oauth_token").get.head
                var authVerifier: String = request.queryString.get("oauth_verifier").get.head

                // AccessTokenを取得する
                // val accessToken: AccessToken = twitter.getOAuthAccessToken(requestToken, authVerifier)
                var token = twitter.getOAuthAccessToken(requestToken, authVerifier)
                println(token)
                // AccessTokenを取得する場合
                // val accessToken: AccessToken = twitter.getOAuthAccessToken(requestToken, authVerifier)
                // val accessTokenSt: String = accessToken.getToken
                // val accessTokenSecretSt: String = accessToken.getTokenSecret

                // Twitterオブジェクトの認証
                twitter.verifyCredentials()

                // TwitterのUserオブジェクトを取得
                var user: User = twitter.showUser(twitter.getId())

                // Cacheに設定(3日間有効)
                Cache.set("twitter_user", user, 4320)
                Cache.set("twitter_obj", twitter, 4320)

                // Cacheから削除
                Cache.remove("twitter")
                Cache.remove("requestToken")

              }
              case _ =>
            }
          }
          case _ =>
        }

        // リターン
        Redirect(routes.Application.index)
      }
    }
  }

  /** 
   * ログアウト処理
   */
  def twitterLogout = Action { implicit request =>

    // Cacheから削除
    Cache.remove("twitter_user")

    // リターン
    Redirect(routes.Application.index)
  }
}




