<?php

require_once('for_php7.php');


class knjl072nModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $appHold;
    public $testdiv;
    public $examcourse;
    public $shdiv;
    public $exam_type;
    public $examno;
    public $ObjYear;
    public $sort;
    public $score;

    public function knjl072nModel()
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
        $this->examcourse   = strlen(VARS::post("EXAMCOURSE")) ? VARS::post("EXAMCOURSE") : VARS::post("HID_EXAMCOURSE");
        $this->shdiv        = strlen(VARS::post("SHDIV")) ? VARS::post("SHDIV") : VARS::post("HID_SHDIV");

        $this->sort         = VARS::post("SORT");
        $this->hid_examno   = VARS::post("HID_EXAMNO");

        $this->field = array("JUDGEMENT" => VARS::post("JUDGEMENT"));
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->score = array();
        //入力項目を受験番号で再配置
        $tmp = explode(",", $this->hid_examno);
        foreach ($tmp as $key => $examno) {
            foreach ($this->field as $col => $val) {
                $this->score[$examno][$col] = $val[$key];
            }
        }
        //名称マスタの取得（合否コード）
        $db = Query::dbCheckOut();
        $opt_judge = array();
        $result = $db->query(knjl072nQuery::getNameMstL013("L013", $this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_judge[] = $row["NAMECD2"];
        }
        Query::dbCheckIn($db);
        //合否コード値チェック
        foreach ($this->score as $examno => $val) {
            if ($val["JUDGEMENT"] != "" && !in_array($val["JUDGEMENT"], $opt_judge)) {
                $this->setWarning("MSG901", "( 合否 )\\n\\n受験番号：".$examno);
                return false;
            }
        }
        //更新処理
        $result = knjl072nQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
