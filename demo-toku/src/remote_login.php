<?PHP
/*(localPHP認証⇔localPHP認証間のNoPass認証システム)
(前サーバーから渡されるもの) 
1.user_id(URL)                -> URLへ     ->(認証時) [URL]からrequest
2.passwrd(URL)                -> URLへ     ->(認証時) [URL]からrequest
3.autotoken(サーバー内で作成) -> SetCookie ->(認証時) [cookie]内からrequest(expire時間有り<-ココでセキュリティを確保)

(注意書)
 20170714 setcookieの仕様(InternetExplorerのみの仕様.Firefox/Chromeは無視される？)
      : サーバー時間とクライアント時間が指定時間(setcookieのパラメータ3）以上ズレていると
       クッキーがセットされない(expireとみなされて自動ログインはされないでログイン画面の表示になる)
        <-- 現在は10分にセットしておく*/

function remotelogin()
{
    $user_id = $_REQUEST['autouser'];
        Syslog(LOG_INFO, "リモートログイン167-user-id [REQUEST]:{" .$user_id ."}");
        Syslog(LOG_INFO, "クッキーチェックcookie[autotoken]:{".$_COOKIE["autotoken"]."}");

    $passwrd_ = $_REQUEST['autopass'];
        Syslog(LOG_INFO, "リモートログイン167-passwrd [REQUEST]:{" .$passwrd_ ."}");

    $pass2 = $_REQUEST['autopass2'];
        Syslog(LOG_INFO, "リモートログイン167-autopass2 [REQUEST]:{" .$pass2 ."}");

    $passwrd = common::passwdDecode($passwrd_, $pass2, true);
        Syslog(LOG_INFO, "リモートログイン167-passwrd [passwdDecode]:{" .$passwrd ."}");

        Syslog(LOG_INFO, "呼出し変換{" .$user_id."}{".$pass2."：".$passwrd_."=>".$passwrd."}");
    if ((strlen($user_id)>0)&&(strlen($passwrd)>0)) {
        $autotoken = md5(microtime());
        Syslog(LOG_INFO, "リモートログイン167-autotoken-login-[md5]:{" .$autotoken ."}");

        $expire = time() + 10 * 60;    // 10分有効
        setcookie('autotoken', $autotoken, $expire,'/');
        Syslog(LOG_INFO, "リモートログイン167-autotoken-login-[setcookie]:{" .$autotoken ."}");

        $passwrd = md5("$user_id:$passwrd:$autotoken");
        Syslog(LOG_INFO, "リモートログイン167-autotoken-login-[passwrd]:{" .$passwrd ."}");
     
        header("Location: https://".$_SERVER['SERVER_NAME'].REQUESTROOT."/X/KNJXMENU/index.php?autoid0=".$user_id."&autoid1=".$passwrd);
    }
    else {
        header("Location: https://".$_SERVER['SERVER_NAME'].REQUESTROOT."/index.php");
    }
}
remotelogin();
?>
