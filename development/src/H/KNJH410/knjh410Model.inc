<?php

require_once('for_php7.php');

class knjh410Model extends Model {

    var $cmd;
    var $cmdSub;
    var $schregno;
    var $delchk;
    var $dividecd;
    var $auth = array();
    var $auth2;
    var $taitleSort = array();
    var $sort;
    var $staffsort;
    var $datesort;
    var $titlesort;
    var $control_data = array();    //コントロールマスタデータを保持
    var $narrowing;


    function knjh410Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //DB接続
        $db = Query::dbCheckOut();

        $this->delchk  = VARS::request("DELCHK");     //チェック

        $this->cmd      = VARS::request("cmd");
        
        $this->mode     = VARS::request("mode") != "" ? VARS::request("mode") : $this->mode;
        if($this->cmd == ""){
            $this->mode = "ungrd";
        }
        
        $this->year = VARS::request("GRD_YEAR") != "" ? VARS::request("GRD_YEAR") : $this->year;
        $this->semester = VARS::request("GRD_SEMESTER") != "" ? VARS::request("GRD_SEMESTER") : $this->semester;
        if($this->cmd == ""){
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        }
        
        //権限
        $this->auth2 = AUTHORITY;
        
        $this->dividecd = VARS::post("DIVIDECD");
        
        //ボタン表示用ラジオボタン
        $this->btnRadio = VARS::post("btnRadio") != "" ? VARS::post("btnRadio") : $this->btnRadio;
        
        //前回の画面更新時のラジオボタンの値
        $this->preBtnRadio = VARS::post("preBtnRadio") != "" ? VARS::post("preBtnRadio") : "1";

        //コマンドを変えたときに情報表示以外のコマンドになっても同じ情報を出し続けるために
        $this->backData = VARS::post("BACK_DATA");

        //分類の絞り込み
        $this->narrowing = VARS::post("NARROWING");


        //タイトルソート
        $this->setTitleSort();

        if (VARS::get("cmdSub")) {
            $this->cmdSub = VARS::get("cmdSub");
        } elseif ($this->cmd == "subEnd") {
            unset($this->actiondate);
            unset($this->actiondateUpd);
            unset($this->seq);
            unset($this->actiontime);
            unset($this->title);
            unset($this->text);
        }

        if ($this->cmd == "upd") {
            $this->actiondate    = str_replace("-", "/", VARS::get("ACTIONDATE"));
            $this->actiondateUpd = str_replace("-", "/", VARS::get("ACTIONDATE"));
            $this->seq = VARS::get("SEQ");
        } elseif ($this->cmd == "updateSub" || $this->cmd == "insertSub") {
            $this->actiondate = VARS::post("ACTIONDATE");
            $this->actiontime = (VARS::post("ACTIONHOUR")) ? sprintf("%02d",VARS::post("ACTIONHOUR")).":".sprintf("%02d",VARS::post("ACTIONMINUTE")).":00" : "";
            $this->title      = VARS::post("TITLE");
            $this->text       = VARS::post("TEXT");
            if ($this->cmd == "insertSub") {
                $maxseq = array();
                $maxseq = $db->getRow(knjh410Query::getMaxSeq($this->schregno), DB_FETCHMODE_ASSOC);
                $this->seq = ($maxseq["SEQ"]) ? $maxseq["SEQ"] : 1;
            }
        }

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
            $this->auth["CHAIRFLG"]   = $db->getOne(knjh410Query::getAuthChair($this));
            $this->auth["CLUBFLG"]    = $db->getOne(knjh410Query::getAuthClub($this));
            $this->auth["HRCLASSFLG"] = $db->getOne(knjh410Query::getAuthHrclass($this));
            $this->auth["COURSEFLG"]  = $db->getOne(knjh410Query::getAuthCourse($this));

            $this->schoolKind = $db->getOne(knjh410Query::getSchoolKind($this));

        }
        
        if($this->cmd == "edit" && VARS::get("SCHREGNO") == ""){
            $this->schregno = "";
            $this->auth["CHAIRFLG"]   = "";
            $this->auth["CLUBFLG"]    = "";
            $this->auth["HRCLASSFLG"] = "";
            $this->auth["COURSEFLG"]  = "";
            
            $this->btnRadio = 1;
        }
        
        //DB切断
        Query::dbCheckIn($db);

    }

    //タイトルソート
    function setTitleSort()
    {
        $this->sort = (VARS::request("sort")) ? VARS::request("sort") : "DATESORT";
        $sortChange = array("1" => "2", "2" => "1");
        $this->staffsort = (!$this->staffsort) ? "1" : $this->taitleSort["STAFFSORT"]["VALUE"];
        $this->datesort  = (!$this->datesort)  ? "2" : $this->taitleSort["DATESORT"]["VALUE"];
        $this->titlesort = (!$this->titlesort) ? "1" : $this->taitleSort["TITLESORT"]["VALUE"];

        $this->taitleSort["STAFFSORT"] = array("NAME1"  => "登録職員▲",
                                               "NAME2"  => "登録職員▼",
                                               "VALUE"  => ($this->cmd != "STAFFSORTCLICK") ? $this->staffsort : $sortChange[VARS::request("STAFFSORT")],
                                               "ORDER1" => "L1.STAFFNAME_KANA,",
                                               "ORDER2" => "L1.STAFFNAME_KANA DESC,");

        $this->taitleSort["DATESORT"]  = array("NAME1"  => "行動日付▲",
                                               "NAME2"  => "行動日付▼",
                                               "VALUE"  => ($this->cmd != "DATESORTCLICK") ? $this->datesort : $sortChange[VARS::request("DATESORT")],
                                               "ORDER1" => "T1.ACTIONDATE, T1.ACTIONTIME,",
                                               "ORDER2" => "T1.ACTIONDATE DESC, T1.ACTIONTIME DESC,");

        $this->taitleSort["TITLESORT"] = array("NAME1"  => "件名▲",
                                               "NAME2"  => "件名▼",
                                               "VALUE"  => ($this->cmd != "TITLESORTCLICK") ? $this->titlesort : $sortChange[VARS::request("TITLESORT")],
                                               "ORDER1" => "T1.TITLE,",
                                               "ORDER2" => "T1.TITLE DESC,");
    }

    //更新処理
    function &getUpdateModel()
    {
        if (!$this->dataCheck()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    //データチェック
    function dataCheck()
    {
        if (strlen($this->title) > 120) {
            $this->setWarning("MSG915","件名は120バイトまでです。");
            return false;
        }
        if (strlen($this->text) > 700) {
            $this->setWarning("MSG915","内容は700バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function update()
    {
        if ($this->cmd == "insertSub") {
            knjh410Query::insertQuery($this);
        } else {
            knjh410Query::updateQuery($this);
        }
    }

    //編集完了用のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for ($i = 0; $i < get_count($this->delchk); $i++) {
            $delvalue = preg_split("{:}", $this->delchk[$i]);
            $query = knjh410Query::getDeleteQuery($delvalue);

            $db->query($query);
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
?>
