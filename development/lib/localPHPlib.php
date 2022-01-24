<?php

require_once('for_php7.php');


$_PHPLIB = array();
$_PHPLIB["libdir"] = GKLIBDIRECTORY."/PHPlib/";

require_once($_PHPLIB["libdir"] . "session/session4.inc");
require_once($_PHPLIB["libdir"] . "auth/auth4.inc");
//require_once($_PHPLIB["libdir"] . "auth/acl_perm.inc");
require_once($_PHPLIB["libdir"] . "auth/perm.inc");
require_once($_PHPLIB["libdir"] . "user4.inc");
require_once($_PHPLIB["libdir"] . "oohforms.inc");
require_once($_PHPLIB["libdir"] . "of_button.inc");

class Gk_Session extends Session {
    var $classname      = "Gk_Session";
    var $mode           = "user";
    var $cookiename     = "";
    var $lifetime       = 0;
    var $gc_probability = 5;
    var $allowcache     = "nocache";
//    var $trans_id_enabled = false;
}

class Gk_Auth extends Auth {
    var $classname      = "Gk_Auth";
    var $lifetime       =  0;
    var $refresh        = -1;
    var $magic          = "webFrobozzica";
    var $database_class = NULL;
    var $database_table = "user_mst";

    function auth_loginform()
    {
        global $sess;
        global $challenge;
        global $_PHPLIB;

        //自動ログイン //v(^|^)_↓
        $user_id = isset($_REQUEST['autoid0']) ? $_REQUEST['autoid0'] : "";
        $passwrd = isset($_REQUEST['autoid1']) ? $_REQUEST['autoid1'] : "";
        $autotoken = isset($_REQUEST['autotoken']) ? $_REQUEST['autotoken'] : "";
        if ((strlen($user_id)>0)&&(strlen($passwrd)>0)&&(strlen($autotoken)>0)) {
            setcookie('autotoken','',time() - 3600,'/');  //クッキーを削除することでエラー時は通常ログインにする
            $challenge = $autotoken;
            $sess->register("challenge");
            include("../../common/call_Auto.ihtml");
        }
        else {
            $challenge = md5(uniqid($this->magic));
            $sess->register("challenge");
        //include("crcloginform.ihtml");
            include(DOCUMENTROOT."/common/crcloginform.ihtml");
        }
        //v(^|^)_↑
    }

    function auth_preauth()
    {
        $cl = $this->cancel_login;
        global $$cl;

        if ($$cl) {
            if ( $uid = $this->auth_validatelogin() ) {
                return $uid;
            }
        }
        return $this->auth["USERID"];
    }

    function auth_validatelogin()
    {

        global $sess;
        $userid     =& VARS::post("username");
        $response   =& VARS::post("response");
        $challenge  =& VARS::post("challenge");

        $db = Query::dbCheckOut();
        $query = "SELECT MSG_CONTENT FROM MESSAGE_MST WHERE MSG_CD ='MSG999'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $row["MSG_CONTENT"] = str_replace("\r","\\r",$row["MSG_CONTENT"]);
        $row["MSG_CONTENT"] = str_replace("\n","\\n",$row["MSG_CONTENT"]);

        $warning = "MSG999\\r\\n\\r\\n".$row["MSG_CONTENT"];

        if (!$sess->is_registered("challenge")) {
            $this->warning = $warning;
        	Query::dbCheckIn($db);
            return false;
        }

        //2017/07/10追加
        //SCHOOLKINDとSCHOOLCDを使うかの判断
        $properties["useSchool_KindMenu"] = "";

        $arr_useUnAdminMenuPrgid = array();
        $arr_useSubMenuId = array();
        $retVal = "";

        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/menuInfo.properties")) {
            $filename = CONFDIR ."/menuInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/menuInfo.properties";
        }
        
        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp,1024)) {
            foreach ($properties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $properties[$key] = str_replace("\r\n", "", $retVal);
                }
            }
        }
        fclose($fp);
        //2017/07/10追加ここまで
        

        $query = "SELECT ";
        $query .= "       T2.USERID, ";
        $query .= "       T2.PASSWD, ";
        $query .= "       T2.INVALID_FLG, ";
        $query .= "       T1.STAFFCD, ";
        $query .= "       T1.STAFFNAME_SHOW ";
        $query .= "  FROM V_STAFF_MST T1, USER_MST T2 ";
        $query .= " WHERE T1.STAFFCD = T2.STAFFCD";
        $query .= " AND   T2.USERID = '" .$userid ."'";
        //2017/07/10追加
        //上で取得したSCHOOLKINDとSCHOOLCDを使うかの分岐
        if($properties["useSchool_KindMenu"] == "1"){
            $query .= " AND   T2.SCHOOLCD = '".SCHOOLCD."'";
            $query .= " AND   T2.SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)) {
            $userid  = $row["USERID"];
            $pass    = $row["PASSWD"];
            $invalid_flg = $row["INVALID_FLG"];
            $this->auth = $row;
            $this->auth["uid"]      = $row["STAFFCD"];
            $this->auth["uname"]    = $row["USERID"];
            $this->auth["name"]     = $row["STAFFNAME_SHOW"];
        }

        Query::dbCheckIn($db);

        $exspected_response = md5("$userid:$pass:$challenge");
        if ($exspected_response != $response || $invalid_flg =='1') {
            if($invalid_flg =='1'){
            $this->warning = "ログインIDが無効です。";
            }else{
            $this->warning = $warning;
            }

            $uid = strlen($userid) ? $userid : "null_id";
            common::access_log("login", 1, "KNJXMENU", $uid);

            return false;

        } else {
            return $userid;
        }
    }

    function isWarning()
    {
        return ($this->warning) ? $this->warning : "";
    }
}

class Gk_Perm extends Perm {
    var $classname = "Gk_Perm";
    var $permissions = array("admin" => 1,
                             "guest" => 2);

    function perm_invalid($does_have, $must_have)
    {
      global $perm, $auth, $sess;
      global $_PHPLIB;

      include($_PHPLIB["libdir"] . "perminvalid.ihtml");
    }
}

class Sess {

    function page_open($feature)
    {
        global $_PHPLIB;
        # enable sess and all dependent features.
        if (isset($feature["sess"])) {
            global $sess;
            $sess = new $feature["sess"];
            $sess->start();

            $GLOBALS["auth"] =& VARS::session("auth");
            # the auth feature depends on sess
            if (isset($feature["auth"]) || is_object($GLOBALS["auth"]) ) {

                global $auth;

                if ( VARS::request("logout") ) {
                   //アクセスログ登録
                    common::access_log("logout", 0);
                    $auth->logout();
                    $url = preg_replace("/([&?])"."logout=true"."(&|$)/",
                                        "\\1", getenv("REQUEST_URI"));

                    $sess->delete();
                    header("Location: $url");
                    exit;
                }
                if (is_object($auth)) {
                    $cl = $auth->cancel_login;
                    global $$cl;
                    if($$cl) $auth->unauth();
                }

                if (!is_object($auth)) {
                    $auth = new $feature["auth"];
//start login K.M
                }
                $auth->database_class = NULL;
                $auth->start();

                # the perm feature depends on auth and sess
                if (isset($feature["perm"])) {
                    global $perm;

                    if (!isset($perm)) {
                        $perm = new $feature["perm"];
                    }
                }

                # the user feature depends on auth and sess
                if (isset($feature["usess"])) {
                    global $usess;

                    if (!isset($usess)) {
                        $usess = new $feature["usess"];
                    }
                    $usess->start($auth->auth["uid"]);
                }

            }

            ## Load the auto_init-File, if one is specified.
            if (($sess->auto_init != "") && !$sess->in) {
                $sess->in = 1;
                include($_PHPLIB["libdir"] . $sess->auto_init);
            }
        }
    }

    function page_close() {
        global $sess, $usess;

        if (isset($sess)) {
            if (isset($usess)) {
                $usess->freeze();
            }
        }
    }
}

?>
