<?php

require_once('for_php7.php');


class knjl670aModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $appHold;
    public $testdiv;
    public $coursecode;
    public $exam_type;
    public $hid_receptno;
    public $ObjYear;
    public $sort;
    public $leastScore;
    public $topScore;
    public $score;
    public $data;
    public $coursecd;
    public $majorcd;
    public $perfectKeishaCount;

    public function knjl670aModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold      = VARS::post("APP_HOLD");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = "1";
        $this->coursecode   = strlen(VARS::post("COURSECODE")) ? VARS::post("COURSECODE") : VARS::post("HID_COURSECODE");

        //念のため、逆に入れられても対応できるようにする。
        $this->leastScore   = VARS::post("LEAST_SCORE") != "" ? ( (VARS::post("TOP_SCORE") != "" && VARS::post("LEAST_SCORE") > VARS::post("TOP_SCORE")) ? VARS::post("TOP_SCORE") : VARS::post("LEAST_SCORE")): "";
        $this->topScore     = VARS::post("TOP_SCORE") != "" ? ((VARS::post("TOP_SCORE") != "" && VARS::post("LEAST_SCORE") > VARS::post("TOP_SCORE")) ? VARS::post("LEAST_SCORE") : VARS::post("TOP_SCORE")) : "";

        $this->rejectPass   = VARS::post("REJECT_PASS");
        $this->rejectUnPass = VARS::post("REJECT_UNPASS");
        //ソートイベントの時だけ入れ替えるようにする。
        $this->sort         = VARS::post("HID_SORT") != "" ? ($this->cmd == "sort" ? (VARS::post("HID_SORT") == "2" ? "0" : VARS::post("HID_SORT") + 1) : VARS::post("HID_SORT")): "";
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");
        $this->checkAll     = VARS::post("CHECKALL");

        $this->field = array("JUDGEDIV" => VARS::post("JUDGEDIV"));

        $this->coursecd = "1";
        $this->majorcd = "100";

        $this->data = array();
        //画面上の検索条件が変わったら、選択状態を保持する意味が無いので、それ以外でのみ選択状態を保持(※取消も対象)
        if ($this->cmd != "app" && $this->cmd != "main" && $this->cmd != "load" && $this->cmd != "reload") {
            $array       = explode(",", $this->hid_receptno);
            foreach ($array as $key => $receptno) {
                $this->data["CHECKED"][$receptno] = VARS::post("CHECKED_".$receptno);
                $this->data["JUDGEDIV"][$receptno] = VARS::post("JUDGEDIV-".$receptno);
            }
        }

        if (strlen($this->coursecode)) {
            // コースの満点マスタに傾斜配点があるか
            $db = Query::dbCheckOut();
            $query = knjl670aQuery::getPerfectKeishaCount($this);
            $this->perfectKeishaCount = $db->getOne($query);
            Query::dbCheckIn($db);
        }
    }

    //教育課程コード系を確定する。
    public function setCourseCdMajorCd()
    {
        if ($this->applicantdiv == "1") {
            $this->coursecd = "0";
            $this->majorcd = "001";
        } else {
            $this->coursecd = "1";
            $this->majorcd = "100";
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //変わっている可能性があるので、設定しなおしておく。
        $this->setCourseCdMajorCd();

        $this->score = array();
        //備考・評価を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            //チェックが付いてる箇所の値を取得する。
            if (VARS::post("CHECKED_".$receptno) == "1") {
                $this->score[$receptno]["JUDGEDIV"] = VARS::post("JUDGEDIV-".$receptno);
            }
        }
        //名称マスタの取得（判定コード）
        $db = Query::dbCheckOut();
        $opt_judge = array();
        $result = $db->query(knjl670aQuery::getNameMst2("L013", $this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_judge[] = $row["NAMECD2"];
        }
        Query::dbCheckIn($db);
        //判定コード値チェック
        foreach ($this->score as $receptno => $val) {
            foreach ($val as $col => $score) {
                if ($col == "JUDGEDIV" && $score != "" && !in_array($score, $opt_judge)) {
                    $this->setWarning("MSG901", "( 判定 )\\n\\n受験番号：".$receptno);
                    return false;
                }
            }
        }
        //更新処理
        $result = knjl670aQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201", $prtStr);
        }
    }
}
