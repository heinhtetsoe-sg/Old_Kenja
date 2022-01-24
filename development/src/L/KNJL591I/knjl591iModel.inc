<?php
class knjl591iModel extends model
{
    public $cmd;
    public $objYear;
    public $applicantdiv;
    public $startHuban;
    public $examnoArray;
    public $studentnoArray;

    public function knjl591iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->cmd = VARS::request("cmd");
        $this->objYear = CTRL_YEAR + 1;
        $this->applicantdiv = "2";
        $this->studentnoArray = array();

        //学科
        $this->majorcd = VARS::post("MAJORCD");

        //開始学籍番号
        $this->startHuban = VARS::post("START_HUBAN");
        if (VARS::post("START_HUBAN") != "") {
            $this->startHuban = sprintf("%03d", $this->startHuban);
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        foreach ($this->examnoArray as $key => $examno) {
            $postStudentno = VARS::post("STUDENTNO_{$examno}");
            if ($postStudentno != "") {
                //入力されている場合、8桁ゼロ埋め
                $postStudentno = sprintf("%08d", $postStudentno);
            }
            $this->studentnoArray[$examno] = $postStudentno;
        }

        knjl591iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
