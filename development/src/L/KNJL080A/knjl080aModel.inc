<?php

require_once('for_php7.php');

class knjl080aModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examType;
    public $ObjYear;

    public function knjl080aModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->hid_ReceptNo);
        }

        $this->ObjYear  = CTRL_YEAR+1;
        $this->examType = 1;    //固定

        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->sort         = VARS::post("SORT");
        $this->passCourse   = VARS::post("PASS_COURSE");
        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");

        $this->cmd = VARS::request("cmd");

        //校種変換用
        if ($this->applicantdiv == "") {
            $db = Query::dbCheckOut();
            $this->skArray = array();
            $query = knjl080aQuery::getNameMst($this->ObjYear, 'L003');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->skArray[$row["VALUE"]] = $row["NAMESPARE3"];
            }
            Query::dbCheckIn($db);
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //初期化
        $this->setReceptNo = $this->arrExamNo = $this->arrTestDiv = $this->arrInputShDiv = $this->arrDefaultScore = array();

        //再配置
        $this->setReceptNo = explode(",", $this->hid_ReceptNo);
        foreach ($this->setReceptNo as $key => $receptno) {
            $this->arrExamNo[$receptno]         = VARS::post("EXAMNO-".$receptno);
            $this->arrTestDiv[$receptno]        = VARS::post("TESTDIV-".$receptno);
            $this->arrInputShDiv[$receptno]     = VARS::post("INPUT_SHDIV-".$receptno);
            $this->arrDefaultScore[$receptno]   = VARS::post("DEFAULT_SCORE-".$receptno);
            $this->arrCoursecd[$receptno][1]    = VARS::post("COURSECD1-".$receptno);
            $this->arrCoursecd[$receptno][2]    = VARS::post("COURSECD2-".$receptno);
        }

        if (VARS::post("HID_PASS_COURSE")) {
            $this->hidPassCourse = explode(",", VARS::post("HID_PASS_COURSE"));
        }

        //更新
        knjl080aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
