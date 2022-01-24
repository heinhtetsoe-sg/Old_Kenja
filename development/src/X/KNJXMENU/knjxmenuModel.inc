<?php

require_once('for_php7.php');
define("DEF_TMPPATH", "/tmp/gaku");
define("DEF_TIMEOUT", 30000);

//メニュー画面
class knjxmenuModel extends model
{
    public $cmd;                   //一覧表示時のcmd
    public $root = "C1000";        //ルートのメニュー(学籍管理システム)
    public $menuid;                //メニューID
    public $programid;             //プログラムID
    public $menuname;
    public $staffcd;
    public $response;
    public $password1;
    public $password2;
    public $mode;
    public $mode1;
    public $control;           //コントロールマスタ
    public $pastyear;          //過去年度フラグ
    public $syukketuDiv;
    public $syukketuSdate;
    public $syukketuEdate;
    public $properties = array();

    //コンストラクタ
    public function knjxmenuModel()
    {
        $this->menuEnglish = "";
        $this->notDisp = "1";
    }

    public function init()
    {
        global $auth;
        
        //プロパティファイル
        $this->getProperties();

        //crcloginform.ihtmlから取得
        if (VARS::post("user_platform")) {
            $this->ua = VARS::post("user_platform");
        }
        $this->ua = $this->getTabletIpAddr();

        //権限が変更された場合
        if ($this->staffcd != STAFFCD) {
            $this->clear();    //初期化
            $this->staffcd      = STAFFCD;                  //職員コード
        }
        //学期変更
        if (VARS::post("CTRL_SEMESTER")) {
            common::GetControlMaster_Fnc($args);
            common::GetControlMaster_Fnc($this->control);
            list($y, $s) = explode(",", VARS::post("CTRL_SEMESTER"));
            $auth->auth["CTRL_YEAR"] = $y;
            $auth->auth["CTRL_SEMESTER"] = $s;
            $auth->auth["SEMESTERNAME"] = $args["学期名"][$s];
        }
        if ($this->control["年度"] > $auth->auth["CTRL_YEAR"]) {
            $this->pastyear = true;
        } else {
            $this->pastyear = false;
        }
        //処理日付変更
        if (VARS::post("CTRL_DATE") && preg_match("/[0-9]{2}\/[0-9]{2}\/[0-9]{2}/", VARS::post("CTRL_DATE"))) {
            $auth->auth["CTRL_DATE"] = str_replace("/", "-", VARS::post("CTRL_DATE"));
        }
        $this->cmd      = VARS::request("cmd");
        if (VARS::request("PROGRAMID")) {
            $this->programid   = VARS::request("PROGRAMID");
        }
        if (VARS::post("ROOT")) {
            $this->root     = VARS::post("ROOT");      //ルートのメニューID
        }
        if (VARS::get("MENUNAME")) {
            $this->menuname   = VARS::get("MENUNAME");
        }
        if (VARS::post("IPADDRESS")) {
            $this->ip         = VARS::post("IPADDRESS");  //送信先のIPアドレス
        }
        if (VARS::post("MESSAGE")) {
            $this->msg        = VARS::post("MESSAGE");    //送信メッセージ
        }
        if (VARS::get("mode")) {
            $this->mode   = VARS::get("mode");
        }
        $this->mode1   = VARS::get("mode1");


        if ($this->cmd == "main" || $this->cmd == "remain") {
            if ($this->menuMode != "SUB") {
                $this->menuid = VARS::request("MENUID") != "" ? VARS::request("MENUID") : "TOP";
                $this->Radio = VARS::request("MENUID") != "" ? substr(VARS::request("MENUID"), 0, 1) : "TOP";
            } else {
                $this->menuid = VARS::request("MENUID") != "" ? VARS::request("MENUID") : "TOP";
                $this->Radio = VARS::request("MENUID") != "" ? substr(VARS::request("MENUID"), 0, 2) : "TOP";
            }
        } elseif ($this->cmd == "online" || $this->cmd == "chg_pwd") {
            $this->menuid = VARS::request("MENUID") != "" ? VARS::request("MENUID") : "TOP";
            $this->Radio = VARS::request("MENUID") != "" ? substr(VARS::request("MENUID"), 0, 1) : "TOP";
        } else {
            $this->menuid = $this->menuid;
            $this->Radio = $this->Radio;
        }

        //パスワード変更用
        $this->response = VARS::post("response");
        $this->password1 = VARS::post("password1");
        $this->password2 = VARS::post("password2");

        $this->title        = VARS::get("TITLE");    //子画面タイトル
        $this->main_src     = VARS::get("main_src");    //子画面URL

        if ($this->cmd == "misyukketu") {
            $this->syukketuDiv = VARS::get("syukketuDiv");
        }
        
        $db = Query::dbCheckOut();
        
        //メニュー表示切替
        if (VARS::request("MENUMODE") != "") {
            $this->menuMode = VARS::request("MENUMODE");
            $this->menuid = "TOP";
            $this->Radio = "TOP";
            
            //STAFF_DETAIL_SEQ_MST更新
            $this->menuUpdate = array();
            $this->menuUpdate["FIELD4"] = $this->menuMode;
            $this->menuUpdate["STAFFCD"] = STAFFCD;
            $this->menuUpdate["STAFF_SEQ"] = '001';
            $this->menuUpdate["REGISTERCD"] = STAFFCD;
            $this->menuUpdate["UPDATED"] = "sysdate()";
            $where = " WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
            
            //DBにデータがあるかどうか commonを使わないで更新挿入する
            $CheckQuery = knjxmenuQuery::checkSdsm($where);
            $rowCnt = $db->getOne($CheckQuery);
            if ($rowCnt > 0) {
                //更新
                $updateQuery = knjxmenuQuery::updateQuery("4", $this->menuUpdate, $where);
                $db->query($updateQuery);
            } else {
                //挿入
                $insertQuery = knjxmenuQuery::insertQuery("4", $this->menuUpdate);
                $db->query($insertQuery);
            }
        } else {
            if ($this->menuMode == "") {
                //初期値を取得
                $query = knjxmenuQuery::getStDetail("4");
                $menuFirst = $db->getOne($query);
                if ($menuFirst != "") {
                    $this->menuMode = $menuFirst;
                } else {
                    $this->menuMode = "MAIN";
                }
            } else {
                $this->menuMode = $this->menuMode;
            }
        }
        
        //CSS切り替え用に変数追加、DBからとってくるように
        if ($this->cssNo == "") {
            $cssQuery = knjxmenuQuery::getStDetail("1");
            $cssNo = $db->getOne($cssQuery);
            if ($cssNo != "") {
                $this->cssNo = $cssNo;
            } else {
                $this->cssNo = $this->properties["cssIRO"];
            }
        } elseif (VARS::request("CSSNO") != "") {
            $this->cssNo = VARS::request("CSSNO");
            //データがあったら更新、なかったら挿入する
            $this->cssup["STAFFCD"] = STAFFCD;
            $this->cssup["STAFF_SEQ"] = '001';
            
            $this->cssup["FIELD1"] = $this->cssNo;
            $this->cssup["REGISTERCD"] = STAFFCD;
            $this->cssup["UPDATED"] = "sysdate()";
            $where = " WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";
            
            //DBにデータがあるかどうか commonを使わないで更新挿入する
            $CheckQuery = knjxmenuQuery::checkSdsm($where);
            $rowCnt = $db->getOne($CheckQuery);
            if ($rowCnt > 0) {
                //更新
                $updateQuery = knjxmenuQuery::updateQuery("1", $this->cssup, $where);
                $db->query($updateQuery);
            } else {
                //挿入
                $insertQuery = knjxmenuQuery::insertQuery("1", $this->cssup);
                $db->query($insertQuery);
            }
        }
        //文字サイズ変更用  1(標準)or2(拡大)
        if ($this->size == "") {
            $sizeQuery = knjxmenuQuery::getStDetail("2");
            $size = $db->getOne($sizeQuery);
            if ($size != "") {
                $this->size = $size;
            } else {
                $this->size = 1;
            }
        } elseif (VARS::request("SIZE") != "") {
            $this->size = VARS::request("SIZE");
            //データがあったら更新、なかったら挿入する
            $this->sizeup["STAFFCD"] = STAFFCD;
            $this->sizeup["STAFF_SEQ"] = '001';

            $this->sizeup["FIELD2"] = $this->size;
            $this->sizeup["REGISTERCD"] = STAFFCD;
            $this->sizeup["UPDATED"] = "sysdate()";
            $where = " WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";

            //DBにデータがあるかどうか commonを使わないで更新挿入する
            $CheckQuery = knjxmenuQuery::checkSdsm($where);
            $rowCnt = $db->getOne($CheckQuery);
            if ($rowCnt > 0) {
                //更新
                $updateQuery = knjxmenuQuery::updateQuery("2", $this->sizeup, $where);
                $db->query($updateQuery);
            } else {
                //挿入
                $insertQuery = knjxmenuQuery::insertQuery("2", $this->sizeup);
                $db->query($insertQuery);
            }
        }
        //言語変更用  0：日本語 1：英語
        if (strlen(VARS::get("LANG")) > 0 && $this->cmd == "change") {
            $this->lang = VARS::request("LANG");
            //データがあったら更新、なかったら挿入する
            $this->langUp["STAFFCD"] = STAFFCD;
            $this->langUp["STAFF_SEQ"] = '001';

            $this->langUp["FIELD5"] = $this->lang;
            $this->langUp["REGISTERCD"] = STAFFCD;
            $this->langUp["UPDATED"] = "sysdate()";
            $where = " WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";

            //DBにデータがあるかどうか commonを使わないで更新挿入する
            $CheckQuery = knjxmenuQuery::checkSdsm($where);
            $rowCnt = $db->getOne($CheckQuery);
            if ($rowCnt > 0) {
                //更新
                $query = knjxmenuQuery::updateQuery("5", $this->langUp, $where);
            } else {
                //挿入
                $query = knjxmenuQuery::insertQuery("5", $this->langUp);
            }
            if ($this->lang != "1") {
                $this->menuEnglish = 0;     //日本語
                $this->menuField = "";
            } else {
                $this->menuEnglish = 1;     //英語
                $this->menuField = "_ENG";
            }
            $db->query($query);
        } else {
            $engQuery = knjxmenuQuery::getStDetail("5");
            $english = $db->getOne($engQuery);
            if ($english != "1") {
                $this->menuEnglish = 0;     //日本語
                $this->menuField = "";
            } else {
                $this->menuEnglish = 1;     //英語
                $this->menuField = "_ENG";
            }
        }

        // 表示表示
        if (strlen(VARS::get("DISP")) > 0 && $this->cmd == "change") {
            $this->notDisp = VARS::get("DISP");
        }
        if (strlen(VARS::get("DATA_DIV_NO")) > 0 && $this->cmd == "checkNotDisp") {
            $divNo = VARS::get("DATA_DIV_NO");
            list($this->dataDiv, $this->oshiraseNo) = explode("_", $divNo);
            $this->insertFlg = VARS::get("insertFlg");
            //表示、非表示の更新
            $deleteQuery = knjxmenuQuery::notDispDelQuery($this);
            $db->query($deleteQuery);
            if ($this->insertFlg == '1') {
                $insertQuery = knjxmenuQuery::notDispInsQuery($this);
                $db->query($insertQuery);
            }
        }

        //入力変換  1(IME)or2(ATOK)
        if ($this->input == "") {
            $inputQuery = knjxmenuQuery::getStDetail("3");
            $input = $db->getOne($inputQuery);
            if ($input != "") {
                $this->input = $size;
            } else {
                $this->input = 1;
            }
        } elseif (VARS::request("INPUT") != "") {
            $this->input = VARS::request("INPUT");
            //データがあったら更新、なかったら挿入する
            $this->inputup["STAFFCD"] = STAFFCD;
            $this->inputup["STAFF_SEQ"] = '001';

            $this->inputup["FIELD3"] = $this->input;
            $this->inputup["REGISTERCD"] = STAFFCD;
            $this->inputup["UPDATED"] = "sysdate()";
            $where = " WHERE STAFFCD = '".STAFFCD."' AND STAFF_SEQ = '001' ";

            //DBにデータがあるかどうか commonを使わないで更新挿入する
            $CheckQuery = knjxmenuQuery::checkSdsm($where);
            $rowCnt = $db->getOne($CheckQuery);
            if ($rowCnt > 0) {
                //更新
                $updateQuery = knjxmenuQuery::updateQuery("3", $this->inputup, $where);
                $db->query($updateQuery);
            } else {
                //挿入
                $insertQuery = knjxmenuQuery::insertQuery("3", $this->inputup);
                $db->query($insertQuery);
            }
        }
        Query::dbCheckIn($db);

        $this->nowpid = VARS::post("nowpid");
        $this->nowcid = VARS::post("nowcid");
        if (VARS::get("kenjaMenuId")) {
            $this->setSchoolWareMenuId(VARS::get("kenjaMenuId"));
        }
    }
    public function setSchoolWareMenuId($kenjaMenuId)
    {
        $db = Query::dbCheckOut();

        //スクールウェアからのMENUID
        list($prgId, $atDate, $peri, $chair) = explode("_", $kenjaMenuId);
        $this->getSchoolWareMenuId = $kenjaMenuId;
        $query = knjxmenuQuery::getMenuMstRow($prgId);
        $this->schoolWareMenu = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
    }
    public function clear()
    {
        unset($this->menuid);
        unset($this->programid);
        unset($this->root);
    }
    /**
     * パスワード変更用モデルを返します
    */
    public function &getUpPwdModel()
    {
        $auth =& VARS::session('auth');
        $userid = $auth->auth["USERID"];
        $pass   = $auth->auth["PASSWD"];
        $challenge = $_SESSION["challenge"];
        $exspected_response = md5("$userid:$pass:$challenge");
        if ($this->response != $exspected_response) {
            echo "<script language=\"JavaScript\">\n";
            echo "  alert('" .$this->errorMessage("MSG999", '旧パスワードに誤りがあります。') ."');\n";
            echo "</script>\n";
            return false;
        }
        if ($this->password1 != $this->password2) {
            echo "<script language=\"JavaScript\">\n";
            echo "  alert('" .$this->errorMessage("MSG999", '再入力されたパスワードが一致しません。') ."');\n";
            echo "</script>\n";
            return false;
        }
        if (preg_match("/\*/", $this->password1)) {
            echo "<script language=\"JavaScript\">\n";
            echo "  alert('" .$this->errorMessage("MSG999", '記号(*)があるため、パスワードは更新されません。') ."');\n";
            echo "</script>\n";
            return false;
        }
        
        list($passwordOk, $passPatternMessage) = $this->checkPassword();
        if ($passwordOk == false) {
            echo "<script language=\"JavaScript\">\n";
            echo "  alert('" .$this->errorMessage("MSG999", 'パスワードは【'.$passPatternMessage.'】で入力して下さい。') ."');\n";
            echo "</script>\n";
            return false;
        }
        //パスワードの履歴があるもしくは禁止回数以下ならばエラー
        list($histCnt, $changeCount) = knjxmenuQuery::getCheckPassHist($this);
        if ($histCnt > 0) {
            if ($changeCount > 0) {
                $message = '過去に使用したパスワードは'.$changeCount.'回以内は入力できません。';
            } else {
                $message = '過去に使用したパスワードは入力できません。';
            }
            echo "<script language=\"JavaScript\">\n";
            echo "  alert('" .$this->errorMessage("MSG999", $message) ."');\n";
            echo "</script>\n";
            return false;
        //パスワードの履歴がなければ、履歴にINSERT
        //パスワードの履歴があるが禁止回数を超えていれば、履歴の更新日付をUPDATE
        } else {
            $result = knjxmenuQuery::getUpdateInsertHist($this);
        }
        //パスワード変更処理
        knjxmenuQuery::getUpdateQuery($this);
        //セッション変数更新
        $auth->auth["PASSWD"] = md5($this->password1);
        echo "<script language=\"JavaScript\">\n";
        echo "  alert('" .$this->errorMessage("MSG201") ."');\n";
        if ($this->mode=='pass_period') {
            echo "  top.location.href='index.php'\n";
            unset($this->mode);
        } else {
            echo "  top.right_frame.location.href='index.php?cmd=chg_pwd&MENUID=1PASS'\n";
        }
        echo "</script>";
        return true;
    }

    /**
     * パスワードをチェックします
    */
    public function checkPassword()
    {
        //プロパティファイル
        $this->getProperties();

        $passwordCheckOption = $this->properties["passwordCheckOption"];
        $passKeta = 8; // パスワードの桁数
        $useNum = 1;   // 数字を含める
        $useUpper = 1; // 大文字を含める
        $useLower = 1; // 小文字を含める
        if ($passwordCheckOption) {
            $useNum = 0;
            $useUpper = 0;
            $useLower = 0;
            $arr = explode(",", $passwordCheckOption);
            foreach ($arr as $e) {
                $e = trim($e);
                $match = array();
                $rtn = preg_match("/LENGTH\s*=\s*([0-9]+)\s*$/", $e, $match);
                if ($rtn) {
                    $passKeta = $match[1];
                    if ($passKeta == 0) {
                        $passKeta = 8;
                    }
                } elseif ($e == "NUMBER") {
                    $useNum = 1;
                } elseif ($e == "ALPHABET_UPPER") {
                    $useUpper = 1;
                } elseif ($e == "ALPHABET_LOWER") {
                    $useLower = 1;
                }
            }
        }
        // " (keta, useNum, useUpp, useLow) = (".$passKeta.", ".$useNum.", ".$useUpper.", ".$useLower.")<br>";
        //文字チェック（英数字&大文字小文字の混在）
        $patterns = array();
        if ($useNum) { // 数字を使用する
            $patterns[] = "/[0-9]/";
            $message .= "数字";
        }
        if ($useUpper) { // 大文字を使用する
            $patterns[] = "/[A-Z]/";
            if ($message != '') {
                $message .= "&";
            }
            $message .= "英字大文字";
        }
        if ($useLower) { // 小文字を使用する
            $patterns[] = "/[a-z]/";
            if (!$useUpper) {
                if ($message != '') {
                    $message .= "&";
                }
                $message .= "英字";
            }
            $message .= "小文字";
        }
        if (get_count($patterns) > 0) {
            $message = "、".$message;
        }
        $message = $passKeta.'桁以上'.$message;
        if (get_count($patterns) > 1) {
            $message .= "の混在";
        }
        // チェック処理
        $ok = strlen($this->password1) >= $passKeta;
        foreach ($patterns as $pattern) {
            if (!preg_match($pattern, $this->password1)) {
                $ok = false;
            }
        }
        return array($ok, $message);
    }

    /**
     * メイン表示用のモデルを返します
    */
    public function &getMainModel()
    {
    }
    /**
     * ツリー表示用のモデルを返します
    */
    public function &getTreeModel()
    {
        //PWD_HISTの有無を確認
        $haveChgPwdField = knjxmenuQuery::getChgPwdField();
        if ($haveChgPwdField) {
            $isChgPwd = knjxmenuQuery::getPwdHistCnt();
            if ($isChgPwd) {
                echo "<script language=\"JavaScript\">\n";
                echo "alert('" .$this->errorMessage("", "パスワードを変更してください。") ."');\n ";
                echo "top.location.href='index.php?cmd=chg_pwd&mode=pass_period' ;";
                echo "</script>";
            }
        }
        $row = knjxmenuQuery::getCheckPasswordQuery($this->properties);

        //パスワード有効期限がNULLの時期限チェックフラグに関係なくログイン可
        if (!is_null($row["PWDVALIDTERM"]) && 0 < strlen($row["PWDVALIDTERM"]) && strlen($row["REMAININGDAYS"])) {
            if (($row["PWDVALIDTERM"]<$row["REMAININGDAYS"])&&($row["PWDTERMCHK_FLG"]==1)) {
                echo "<script language=\"JavaScript\">\n";
                echo "alert('" .$this->errorMessage(MSG999, "期限が切れています。") ."');\n ";
                echo "top.location.href='index.php?cmd=chg_pwd&mode=pass_period' ;";
                echo "</script>";
            }
            $days = $row["PWDVALIDTERM"] - $row["REMAININGDAYS"];
            if (($days < 7)&&($row["PWDTERMCHK_FLG"]==1)) {
                echo "<script language=\"JavaScript\">\n";
                echo "alert('" .$this->errorMessage("", "ご使用のパスワードの有効期限は【残り{$days}日】です。") ."');\n ";
                echo "</script>";
            }
        }
    }
    /**
     * 年度を変更します
    */
    public function &chgCtrlYear()
    {
        //システム管理者
        if (STAFFCD == '00999999') {
            $ctrl = array();
            $ctrl["CTRL_YEAR"] = $this->chg_year;
            $ctrl["CTRL_SEMESTER"] = $this->chg_semester;
            $ctrl["SEMESTERNAME"] = $this->chg_semestername;

            $id = md5(serialize($ctrl));

            //セッション変数に保存
            $session = new APP_Session($id, 'SESS_CTRL');

            $session->unregister("CTRL_YEAR");
            $session->unregister("CTRL_SEMESTER");
            $session->unregister("SEMESTERNAME");
            $session->register("CTRL_YEAR", $this->chg_year);
            $session->register("CTRL_SEMESTER", $this->chg_semester);
            $session->register("SEMESTERNAME", $this->chg_semestername);

//            header("Location: index.php?logout=true&SESS_CTRL_ID=" .$id);
            exit;
        }
    }
    public function &getHiddenModel()
    {
        echo "<script language=\"JavaScript\" src=\"knjxmenuForm2.js\"></script>\n";
        echo "<script language=\"JavaScript\">\n";
        echo "  window.onunload = function(){logout2();}\n";
        echo "</script>";
    }
    public function &getTopModel()
    {
        View::CheckUser($_SERVER["REMOTE_ADDR"], USERID);
        $count = View::UserCount();
        $fname = DEF_TMPPATH ."/msg_" .md5($_SERVER["REMOTE_ADDR"] ."|" .USERID);
        echo "<script language=\"JavaScript\">\n";
        if (file_exists($fname)) {
            $fp = fopen($fname, "r");
            $msg = fread($fp, filesize($fname));
            $msg = str_replace("\r", "\\r", $msg);
            $msg = str_replace("\n", "\\n", $msg);
            fclose($fp);
            unlink($fname);
            echo    "   alert('$msg');\n";
        }
        echo "  setTimeout('document.forms[0].submit();', " .DEF_TIMEOUT .");\n";
        echo "</script>";
        echo "<form name='TOP'  method='POST' action='index.php' target='_self' >\n";
        echo "<input type=\"hidden\" name=\"cmd\" value=\"top\">\n";
        echo "</form>\n";
        echo "</html>\n";
    }
    //トップ画面のフレーム
    public function &getTopFrameModel()
    {
        $args["rows"]       = "0,*";
        $args["TITLE"]      = $this->title;
        $args["TOP_SRC"]    = 'index.php?cmd=top_src&PROGRAMID=' .$this->programid;
        $args["MAIN_SRC"]   = $this->main_src;
        $args["CHARSET"]    = CHARSET;
        View::t_include("top_frame.html", $args);
    }
    //
    public function &getTopSrcModel()
    {
        $args["PROGRAMID"]  = $this->programid;
//        $args["AUTHORITY"]  = common::SecurityCheck(STAFFCD, $this->programid);
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($this, "hidden.html", $args);
    }
    //起動終了
    public function &getFinishModel()
    {
        //アクセスログ登録
        common::access_log($this->cmd, 0, $this->programid);
    }
    /**
     * Unloadイベント用のモデルを返します
    */
    public function &getUnloadModel()
    {
    }
    /**
     * 現在ログオンしている人を表示
    */
    public function &getOnlineModel()
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]  = $objForm->get_start("MSG", "POST", "index.php", "", "MSG");

        $arg["MENUNAME"] = "メッセージ送信";

        
        //fontsizeによってcssのクラス変える
        if ($this->size != 1) {
            $arg["csssize"] = "big";
        } else {
            $arg["csssize"] = "";
        }


        $db = Query::dbCheckOut();
        //学校名表示
        $gakuQuery = knjxmenuQuery::getGaku($this);
        $gakuRow = $db->getOne($gakuQuery);
        $arg["GAKU_NAME"] = $gakuRow;
        
        Query::dbCheckIn($db);
        
        

        $arg["CTRL_YEAR"] = CTRL_YEAR ."年度&nbsp;" .CTRL_SEMESTERNAME ."&nbsp;&nbsp;&nbsp;処理日付：" .CTRL_DATE;
        $arg["STAFFNAME_SHOW"] = STAFFNAME_SHOW;
        $arg["REQUESTROOT"] = REQUESTROOT;

        if (!$this->mode1) {
            unset($this->ip);
            unset($this->msg);
            $this->msg="";
            $this->ip=array();
        }
        
        $usr_arr = file(USR_LST);
        $opt = array();
        $selected =array();

        for ($i = 0; $i < get_count($usr_arr); $i++) {
            list($ip_addr, $usr, $tim_stmp) = explode("|", $usr_arr[$i]);
            for ($j = 0; $j < get_count($usr_arr); $j++) {
                if (!in_array($j, array_keys($this->ip))) {
                    $this->ip[$j]   = null;
                }

                $ch=md5($ip_addr .'|' .$usr);
                if (($this->ip[$j] == $ch)) {
                    $selected[] = array("label"  => $ip_addr .' ' .$usr,
                                            "value" => md5($ip_addr .'|' .$usr)
                                            );
                }
            }

            $opt[] = array("label"  => $ip_addr .' ' .$usr,
                           "value"  => md5($ip_addr .'|' .$usr)
                           );
        }
        $objForm->ae(array("type"       => "select",
                            "name"       => "IPADDRESS",
                            "multiple"   => true,
                            "size"       => "5",
                            "extrahtml"  => "style=\"font-size:100%;\"",
                            "value"      => $selected,
                            "options"    => $opt));

        $arg["IPADDRESS"] = $objForm->ge("IPADDRESS");

        //拡大したときに横幅が大きくなりすぎないようにする
        if ($this->size != 1) {
            $cols = "30";
        } else {
            $cols = "50";
        }

        $objForm->ae(array("type"      =>  "textarea",
                            "name"      =>  "MESSAGE",
                            "rows"      =>  "10",
                            "cols"      =>  $cols,
                            "extrahtml" =>  "style=\"font-size:120%;\"",
                            "wrap"      =>  "hard",
                            "value"     =>  "$this->msg"));

        $arg["MESSAGE"] = $objForm->ge("MESSAGE");

        //送信ボタンを作成する
        $objForm->ae(array("type"      => "submit",
                            "name"      => "btn_submit",
                            "value"     => "送信"
                            ));

        $arg["btn_submit"] = $objForm->ge("btn_submit");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => "send"
                            ));

        $cssplugin = "kenjaMenu{$this->cssNo}.css|kyotu.css|css/font-awesome.min.css";


        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML4($model, "message.html", $arg, "", $cssplugin);
        
        echo "<script language=\"JavaScript\">\n";
        echo "window.open('index.php?cmd=retree','left_frame');\n";
        echo "</script>";
    }
    /**
     * メッセージ送信
    */
    public function &getSendModel()
    {
        if ($this->msg != '' && $this->ip[0]!='') {
            for ($i = 0; $i < get_count($this->ip); $i++) {
                $fname = DEF_TMPPATH ."/msg_" .$this->ip[$i];
                $fp = fopen($fname, "w");
                $msg = "From : " .USERID ." ".$_SERVER["REMOTE_ADDR"] ."\\n";
                $msg .= "Date : " .date("Y/m/d(D) H:i:s") ."\\n";
                $msg .= "\\r\\n";
                $msg .= addslashes($this->msg);
                fputs($fp, $msg);
                fclose($fp);
            }
            $msg = "メッセージを送信しました。";
            echo "<script language=\"JavaScript\">\n";
            echo "alert('$msg');\n";
            echo "  top.right_frame.location.href='index.php?cmd=online&MENUID=MESS'\n";
            echo "</script>";
        } else {
            echo "<script language=\"JavaScript\">\n";
            if ($this->ip[0]=='') {
                echo "alert('".$this->errorMessage("MSG310", "アドレスが選択されていません。")."');\n";
            } elseif ($this->msg == '') {
                echo "alert('".$this->errorMessage("MSG301", "メッセージが入力されていません。")."');\n";
            }
            $msg = "メッセージを送信できませんでした。";
            echo "alert('$msg');\n";
            echo "  top.right_frame.location.href='index.php?cmd=online&mode1=resend&MENUID=MESS'\n";
            echo "</script>";
        }
    }
    /**
     * 管理者所属は年度変更画面表示
    */
    public function &getChgSemesModel()
    {
        global $auth;
        
        $db = Query::dbCheckOut();
        
        //最初にログインした際に学期変更を行ったかどうかを判断する
        $this->gakkichange = 0;
        
        $query = knjxmenuQuery::getUsergroupQuery($this->properties);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //管理者フラグ設定
        if (is_array($row) || STAFFCD == "00999999") {
            $auth->auth["ADMINGRP_FLG"] = 1;
        } else {
            $auth->auth["ADMINGRP_FLG"] = 0;
        }

        //プロパティファイル
        $this->getProperties();

        // 1:管理者のみ 2:全員
        if ($this->properties["useChgSemesUser"] == "1" && $auth->auth["ADMINGRP_FLG"] == 0) {
            return;
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"]  = $objForm->get_start("SEMESTER", "POST", "index.php", "", "SEMESTER");

        //変更年度・学期取得
        $result = $db->query(knjxmenuQuery::selectChgTermQuery($auth->auth["ADMINGRP_FLG"], $this->properties));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"] ."年　" .htmlspecialchars($row["SEMESTERNAME"]),
                           "value" => $row["YEAR"] ."," .$row["SEMESTER"]
                           );
        }
        
        //表示を英語にするか
        if ($this->menuEnglish != 1) {
            $arg["Chg_Semes"] = "学期変更";
            $arg["Chg_Semes_YEAR"] = "年度・学期";
            $arg["Chg_Semes_DATE"] = "処理日付";
        } else {
            $arg["Chg_Semes"] = "Change Semester";
            $arg["Chg_Semes_YEAR"] = "Year・Semester";
            $arg["Chg_Semes_DATE"] = "Date";
        }

                       
        $objForm->ae(array("type"       => "select",
                            "name"       => "CTRL_SEMESTER",
                            "size"       => "3",
                            "extrahtml"  => "",
                            "value"      => CTRL_YEAR .",".CTRL_SEMESTER ,
                            "options"    => $opt));

        $arg["CTRL_SEMESTER"] = $objForm->ge("CTRL_SEMESTER");

        if ($this->cmd == "gakki") {
            //送信ボタンを作成する
            if ($this->menuEnglish != 1) {
                $val = "決　定";
            } else {
                $val = "O K";
            }
            $objForm->ae(array("type"      => "submit",
                                "name"      => "btn_submit",
                                "value"     => $val
                                ));

            $arg["btn_submit"] = $objForm->ge("btn_submit");
        } else {
            //送信ボタンを作成する
            if ($this->menuEnglish != 1) {
                $val = "ログイン";
            } else {
                $val = "Login";
            }
            $objForm->ae(array("type"      => "submit",
                                "name"      => "btn_submit",
                                "value"     => $val
                                ));

            $arg["btn_submit"] = $objForm->ge("btn_submit");

            //キャンセルボタンを作成する
            if ($this->menuEnglish != 1) {
                $val = "キャンセル";
            } else {
                $val = "Cancel";
            }
            $objForm->ae(array("type"        => "button",
                                "name"        => "btn_cancel",
                                "value"       => $val,
                                "extrahtml"   => "onclick=\"top.location.href='index.php?logout=true'\"" ));

            $arg["btn_cancel"] = $objForm->ge("btn_cancel");
        }
        //処理日付
        $arg["CTRL_DATE"] = View::popUpCalendar($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
        $arg["finish"]  = $objForm->get_finish();

        if ($this->ua == "tablet") {
            $arg["zoom"] = "document.body.style.zoom = 1.5;";
        }
        
        $cssplugin = "kenjaMenu{$this->cssNo}.css|kyotu.css";
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML4($this, "knjxmenuSemes.html", $arg, "", $cssplugin);
        $this->gakkichange = 1;
        exit;
    }
    public function getProperties()
    {
        $this->properties = array();
        //初期値
        $this->properties["useChgSemesUser"]        = "1";
        $this->properties["useAdminYearPast"]       = "6";
        $this->properties["useAdminYearFuture"]     = "1";
        $this->properties["useUnAdminYearPast"]     = "6";
        $this->properties["useUnAdminYearFuture"]   = "0";
        $this->properties["useRootMenu"]            = "C1000";
        $this->properties["useCheckAttendUnInput"]  = "OFF";
        $this->properties["useCheckAttendInputPrg"] = "";
        $this->properties["useTuutatu"]             = "OFF";
        $this->properties["useUnAdminMenuPrgid"]    = "";
        $this->properties["useSubMenuId"]           = "";
        $this->properties["DownLoadAdbeRdr"]        = "AdbeRdr60_jpn_full.exe";
        $this->properties["DownLoadJava"]           = "j2re-1_4_2_10-windows-i586-p.exe";
        $this->properties["NotUseMessage"]          = "";
        $this->properties["useMenuUnuseStaffDat"]   = "";
        //$this->properties["useMenuStaffDat"]  = "1";
        $this->properties["useMenuStaffDat"]  = "";
        $this->properties["useImeOrAtok"]  = "";
        $this->properties["cssIRO"] = "2";
        $this->properties["useSchool_KindMenu"] = "";
        $this->properties["useChgSchool"]          = "0";
        $this->properties["useLanguageChange"]  = "";
        $this->properties["passwordCheckOption"] = "";
        $this->properties["oldPasswordEnableChangeCount"] = "";
        $this->properties["userAgent"] = "";

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
        while ($line = fgets($fp, 1024)) {
            foreach ($this->properties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $retVal = str_replace("\n", "", str_replace("\r\n", "", $retVal));
                    $this->properties[$key] = $retVal;
                    if ($key == "useUnAdminMenuPrgid") {
                        $arr_useUnAdminMenuPrgid[] = $retVal;
                    }
                    if ($key == "useSubMenuId") {
                        $arr_useSubMenuId[] = $retVal;
                    }
                }
            }
        }
        fclose($fp);
        $this->root = $this->properties["useRootMenu"];
        if ($this->properties["useUnAdminMenuPrgid"] != "") {
            $this->properties["useUnAdminMenuPrgid"] = implode("','", $arr_useUnAdminMenuPrgid);
        }
        if ($this->properties["useSubMenuId"] != "") {
            $this->properties["useSubMenuId"] = implode("','", $arr_useSubMenuId);
        }

        return $retVal;
    }

    public function getTabletIpAddr()
    {
        //Tabletメニュー用IPファイル
        //形式は下記の３つのみ有効
        //100.100.100.100                   完全一致
        //100.100.100                       部分一致
        //100.100.100.100-100.100.100.140   ４番目の範囲内(1～3番目は一致しているものとする)

        $ipRemoteAddr = $_SERVER['REMOTE_ADDR'];
        
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/tablet_ip.properties")) {
            $filename = CONFDIR ."/tablet_ip.properties";
        } else {
            $filename = DOCUMENTROOT ."/tablet_ip.properties";
        }

        if (!file_exists($filename)) {
            return $this->ua;
        }
        $fp = @fopen($filename, 'r');
        //カンマ
        $sepC = ".";
        //マイナス
        $sepM = "-";
        while ($line = fgets($fp, 1024)) {
            $line = str_replace("\r\n", "", $line);
            $sepCCnt = mb_substr_count($line, $sepC);
            $sepMCnt = mb_substr_count($line, $sepM);
            if ($sepMCnt == "1") {
                list($ip1, $ip2) = preg_split($sepM, $line);
                $sepCCnt1 = mb_substr_count($ip1, $sepC);
                $sepCCnt2 = mb_substr_count($ip2, $sepC);
                if ($sepCCnt1 == "3" && $sepCCnt2 == "3") {
                    list($ip1_1, $ip1_2, $ip1_3, $ip1_4) = preg_split("{\.}", $ip1);
                    list($ip2_1, $ip2_2, $ip2_3, $ip2_4) = preg_split("{\.}", $ip2);
                    if ($ip1_4 > $ip2_4) {
                        continue;
                    }
                    for ($ipNo4 = $ip1_4; $ipNo4 <= $ip2_4; $ipNo4++) {
                        $checkIp = "{$ip1_1}.{$ip1_2}.{$ip1_3}.{$ipNo4}";
                        if ($checkIp == $ipRemoteAddr) {
                            return "tablet";
                        }
                    }
                }
            } elseif ($sepMCnt == "0") {
                if ($sepCCnt == "2") {
                    if (mb_substr_count($ipRemoteAddr, $line) == "1") {
                        return "tablet";
                    }
                } elseif ($sepCCnt == "3") {
                    if ($line == $ipRemoteAddr) {
                        return "tablet";
                    }
                }
            }
        }
        fclose($fp);

        return $this->ua;
    }

    // 奨学金情報のモデルを返します
    public function &getTuutatuUpd()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->TuutatuDocNumber as $key => $docNum) {
            $query = knjxmenuQuery::getUpdTuutatu($this, $docNum);
            $db->query($query);
        }

        if ($query != "") {
            echo $query;
        } else {
            echo "NOTFOUND";
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
