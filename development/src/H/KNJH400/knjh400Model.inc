<?php

require_once('for_php7.php');


class knjh400Model extends Model
{
    public $cmd;
    public $cmdSub;
    public $schregno;
    public $delchk;
    public $dividecd;
    public $narrowing;
    public $auth = array();
    public $taitleSort = array();
    public $sort;
    public $staffsort;
    public $datesort;
    public $titlesort;
    public $control_data = array();    //コントロールマスタデータを保持

    public function knjh400Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $this->delchk  = VARS::request("DELCHK");     //チェック

        $this->cmd      = VARS::request("cmd");

        $this->mode     = VARS::request("mode") != "" ? VARS::request("mode") : $this->mode;
        if ($this->cmd == "") {
            $this->mode = "ungrd";
        }

        if ($this->cmd == "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        }
        if (VARS::request("SCHREGNO") != "" && $this->cmd != "radio") {
            $this->year = VARS::request("GRD_YEAR") != "" ? VARS::request("GRD_YEAR") : CTRL_YEAR;
            $this->semester = VARS::request("GRD_SEMESTER") != "" ? VARS::request("GRD_SEMESTER") : CTRL_SEMESTER;
        }

        $this->dividecd = VARS::post("DIVIDECD");

        //分類の絞り込み
        $this->narrowing = VARS::post("NARROWING");

        //ボタン表示用ラジオボタン
        $this->btnRadio = VARS::post("btnRadio") != "" ? VARS::post("btnRadio") : $this->btnRadio;

        //タイトルソート
        $this->setTitleSort();

        //項目（初期表示）
        $this->setDefaultHead();

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
            $this->actiontime = (VARS::post("ACTIONHOUR")) ? sprintf("%02d", VARS::post("ACTIONHOUR")).":".sprintf("%02d", VARS::post("ACTIONMINUTE")).":00" : "";
            $this->title      = VARS::post("TITLE");
            $this->text       = VARS::post("TEXT");
            if ($this->cmd == "insertSub") {
                $maxseq = array();
                $maxseq = $db->getRow(knjh400Query::getMaxSeq($this->schregno), DB_FETCHMODE_ASSOC);
                $this->seq = ($maxseq["SEQ"]) ? $maxseq["SEQ"] : 1;
            }
        }

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
            $this->auth["CHAIRFLG"]   = $db->getOne(knjh400Query::getAuthChair($this));
            $this->auth["CLUBFLG"]    = $db->getOne(knjh400Query::getAuthClub($this));
            $this->auth["HRCLASSFLG"] = $db->getOne(knjh400Query::getAuthHrclass($this));
            $this->auth["COURSEFLG"]  = $db->getOne(knjh400Query::getAuthCourse($this));
            $this->Pattern20 = '';
            $this->grade = VARS::get("GRADE");
        }

        if ($this->cmd == "edit" && VARS::get("SCHREGNO") == "") {
            $this->schregno = "";
            $this->auth["CHAIRFLG"]   = "";
            $this->auth["CLUBFLG"]    = "";
            $this->auth["HRCLASSFLG"] = "";
            $this->auth["COURSEFLG"]  = "";

            $this->btnRadio = 1;
        }
        if (VARS::post("Pattern20")) {
            $this->Pattern20 = VARS::post("Pattern20");
        }
        //DB切断

        $db = Query::dbCheckOut();
        $this->schKind = $db->getOne(knjh400Query::getSchoolKind($this));
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //タイトルソート
    public function setTitleSort()
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

    //項目（初期表示）
    public function setDefaultHead()
    {
        $this->head = array();
        $this->head["01"]   = array("年組番",       "ATTENDNO");
        $this->head["02"]   = array("課程学科",     "MAJORCD");
        $this->head["03"]   = array("学籍番号",     "SCHREGNO");
        $this->head["04"]   = array("氏名",         "NAME_SHOW");
        $this->head["05"]   = array("氏名かな",     "NAME_KANA");
        $this->head["06"]   = array("英字氏名",     "NAME_ENG");
        $this->head["07"]   = array("年次",         "ANNUAL");
        $this->head["08"]   = array("コース",       "COURSECODE");
        $this->head["09"]   = array("性別",         "SEX");
        $this->head["10"]   = array("クラブ",       "CLUBCD");
        $this->head["11"]   = array("委員会",       "COMMITTEECD");
    }

    //更新処理
    public function &getUpdateModel()
    {
        if (!$this->dataCheck()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    //データチェック
    public function dataCheck()
    {
        if (strlen($this->title) > 120) {
            $this->setWarning("MSG915", "件名は120バイトまでです。");
            return false;
        }
        if (strlen($this->text) > 700) {
            $this->setWarning("MSG915", "内容は700バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    public function update()
    {
        if ($this->cmd == "insertSub") {
            knjh400Query::insertQuery($this);
        } else {
            knjh400Query::updateQuery($this);
        }
    }

    //編集完了用のモデルを返します
    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for ($i = 0; $i < get_count($this->delchk); $i++) {
            $delvalue = preg_split("/:/", $this->delchk[$i]);
            $query = knjh400Query::getDeleteQuery($delvalue);

            $db->query($query);
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
