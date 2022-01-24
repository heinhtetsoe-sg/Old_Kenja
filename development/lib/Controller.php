<?php

require_once('for_php7.php');

// app環境を指定します。 (どのエラーを表示したいか)
define ('DEBUG_ENV', true);
//サブシステムID
define ('KNJ_SUBSYSTEMID', 'KNJ');
/**
 * コントローラー基底クラス
 */
require_once('PEAR.php');
require_once('pear/Application.php');
class Controller extends PEAR {
    var $ModelClassName;
    var $authentication = true;

    // {{{ constructor

    /**
     * Constructor.
     *
     * @access public
     * @return void
     */
    function Controller()
    {
        $this->PEAR();
        $this->init();
        $this->session_start();
        $this->start();
    }

    // }}}
    // {{{ destructor

    /**
     * Destructor
     *
     * @access public
     * @return void
     */
    function _Controller()
    {
        //プログラムIDがサブシステムであるか
        if (isset($this->ProgramID) && preg_match("/^" .KNJ_SUBSYSTEMID."[A-Z]{1}[0-9]{3}$/i", $this->ProgramID)){
            $model =& Model::getModel($this);
            if (is_object($model) && $model->isWarning()){
                unset($model->warning);
                //アクセスログ登録
                common::access_log(VARS::request("cmd"), 0);
            }
        }
    }

    function start()
    {
        if ($this->ModelClassName) {
            $this->main();
        } else {
            $msg = "クラス(".get_class($this).") のプロパティ ModelClassName がセットされていません。";
            $model =& $this->raiseError($msg);
            $this->callView($model, "../error");
        }

        session_write_close();
        @session_start();
    }

    function main()
    {
        ;
    }
    //権限チェック
    function checkAuth($auth, $view="", $sendAuth=""){
        $model =& Model::getModel($this);

        $checkAuth = strlen($sendAuth) > 0 ? $sendAuth : AUTHORITY;
        if ($checkAuth < $auth){
            $model->setWarning("MSG300");
            if ($view == ""){
                $view = $model->_view;
            }
            //直前に表示されたフォームを表示
            $this->callView($view);
            exit;
        }
    }
    function callView($view)
    {
        $model =& Model::getModel($this);
        $model->_view = $view;
        header("Content-Type: text/html; charset=".CHARSET);
        require_once("${view}.php");
        $obj = new $view;
        $obj->main($model);

        unset($model->error);
        unset($model->warning);
        unset($model->message);
    }

    function session_start()
    {

        $options = array();

        $options["sess"] = "Gk_Session";
        if($this->authentication || VARS::request("login")){
            $options["auth"] = "Gk_Auth";
            $options["perm"] = "Gk_Perm";
        }
        Sess::page_open($options);
        global $auth, $sess;

        //認証者のデータを定数にセット
        define("PROGRAMID", $this->ProgramID);
        define("USERID",    $auth->auth["USERID"]);                       //利用者ID
        define("STAFFCD",   $auth->auth["STAFFCD"]);                      //職員コード
        define("STAFFNAME_SHOW",   $auth->auth["STAFFNAME_SHOW"]);        //職員名
        define("STAFFNAME_ENG",  $auth->auth["STAFFNAME_ENG"]);           //職員英語名

        //v(^|^)_↓
        $req_cmd = VARS::request("cmd");
        if (is_object($auth)) {
            if ((empty($req_cmd)) && (VARS::request("close"))) {	//学校切替要求
                $change_url = $_COOKIE["change_school"];           //クッキーに保存している切替先学校のURL取出し
                setcookie('change_school','',time() - 3600,'/'); //クッキー削除
                $name = $auth->auth["USERID"];                     //認証済みオブジェクトからユーザーID取出し
                //パスワード取出し
                $db = Query::dbCheckOut();
                $query = "SELECT ";
                $query .= " PASSWD ";
                $query .= " FROM USER_MST ";
                $query .= " WHERE USERID = '" .$name ."'";
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $autotoken = md5(microtime());
                $pass = "";
                if (is_array($row))
                    $pass .= common::passwdEncode($row["PASSWD"], $autotoken, true);
                else
                    $pass .= common::passwdEncode("PASSWD ERROR", $autotoken, true);
                Query::dbCheckIn($db);
                $request = $change_url."?autouser=".$name."&autopass=".$pass."&autopass2=".$autotoken;
                Syslog(LOG_INFO, "学校切替{" .$request ."}");
                //アクセスログ登録
                common::access_log("logout", 5, "Controller");
                $auth->logout();
                //切替先学校へリンク
                echo "<script language=\"JavaScript\">\n";
                echo "function close(){\n";
                echo "    top.location.href='".$request."';\n";
                echo "}\n";
                echo "close()";
                echo "</script>";
                exit();
            }
        }
        //v(^|^)_↑

        if(VARS::request("login")){
            $db = Query::dbCheckOut();

            //コントロールマスタ取得
            $query = "SELECT DISTINCT ";
            $query .= "  T1.*, ";
            $query .= "  T2.SEMESTERNAME ";
            $query .= "FROM ";
            $query .= "  CONTROL_MST T1, ";
            $query .= "  SEMESTER_MST T2 ";
            $query .= "WHERE ";
            $query .= "  T1.CTRL_YEAR = T2.YEAR AND ";
            $query .= "  T1.CTRL_SEMESTER = T2.SEMESTER ";
            $query .= "ORDER BY ";
            $query .= "  CTRL_NO ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $auth->auth["CTRL_YEAR"]        = $row["CTRL_YEAR"];
            $auth->auth["CTRL_SEMESTER"]    = $row["CTRL_SEMESTER"];
            $auth->auth["CTRL_DATE"]        = $row["CTRL_DATE"];
            $auth->auth["SEMESTERNAME"]     = $row["SEMESTERNAME"];
            $auth->auth["ATTEND_CTRL_DATE"] = $row["ATTEND_CTRL_DATE"];
           
            Query::dbCheckIn($db);
        }
        define("CTRL_YEAR",     $auth->auth["CTRL_YEAR"]);
        define("CTRL_SEMESTER", $auth->auth["CTRL_SEMESTER"]);
        define("CTRL_DATE",     $auth->auth["CTRL_DATE"]);
        define("CTRL_SEMESTERNAME", $auth->auth["SEMESTERNAME"]);
        define("ATTEND_CTRL_DATE", $auth->auth["ATTEND_CTRL_DATE"]);
        define("ADMINGRP_FLG", $auth->auth["ADMINGRP_FLG"]);

        /*
            返り値    : ARG1 - SecurityCheck      - 0 権限無し
            　　　                                  1 参照のみ
            　　　                                  2 更新可
            　　　                                  3 制限付き参照
            　　　                                  4 制限付き更新可
        */
        define("AUTHORITY", common::SecurityCheck(STAFFCD, PROGRAMID));   //セキュリティチェック

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
        
        //管理者
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     USERGROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUPCD = '9999' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        //2017/07/10追加
        //上で取得したSCHOOLKINDとSCHOOLCDを使うかの分岐
        if($properties["useSchool_KindMenu"] == "1"){
            $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $kanriGroup = $db->getOne($query);
        Query::dbCheckIn($db);
        define("IS_KANRISYA", $kanriGroup > 0 || STAFFCD == "00999999" ? true : false);

        if (VARS::request("login")){
           //アクセスログ登録
            common::access_log("login", 0);
        }

        $GLOBALS["app"] = new APP_Session($sess->id,$this->ModelClassName);
    }

    function init()
    {
        ;
    }
}
// この関数がエラーを処理します。
function handle_pear_error ($error_obj) {
    if (DB::isError($error_obj)){
    //アクセスログ登録
        common::access_log(VARS::request("cmd"), 1);
    }
    if (DEBUG_ENV) {
        die ($error_obj->getMessage()."\n".$error_obj->getDebugInfo());
    }else{
        die ('Sorry you request can not be processed now. Try again later');
    }
}
// setErrorHandling(PEAR_ERROR_CALLBACK, array($object,'メソッド名');
PEAR::setErrorHandling(PEAR_ERROR_CALLBACK, 'handle_pear_error');
?>
