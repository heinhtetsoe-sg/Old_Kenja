<?php

require_once('for_php7.php');

class knjl064iModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;
    public $upd_data_base;
    public $upd_data_before;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            $this->field = array();
            unset($this->examyear);
        }
        $this->field = array("APPLICANTDIV" => VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"      => "2",                         //B固定
                             "CENTER_TITLE" => VARS::post("CENTER_TITLE")   //重複チェック項目
                     );
        $this->cmd          = VARS::request("cmd");
        $this->examyear     = CTRL_YEAR + 1;

        $this->upd_data_base    = strlen(VARS::post("upd_data_base")) ? split(',', VARS::post("upd_data_base")) : array();   //志願者データで更新
        $this->upd_data_before  = strlen(VARS::post("upd_data_before")) ? split(',', VARS::post("upd_data_before")) : array();   //下リスト
    }

    public function &getMainModel()
    {
    }

    public function validateInput()
    {
        if (!strlen($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!strlen($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        return true;
    }

    public function &getExecModel()
    {
        if (!$this->validateInput()) {
            return;
        }

        if ($this->cmd == "syougou") {
            $this->nameKanaMatchFlg = true;
            $this->birthDayMatchFlg = true;
            $this->telNoMatchFlg    = true;

            $ret = knjl064iQuery::executeSyougouQuery($this);

            $this->setMessage("MSG202");
            $this->field["CENTER_TITLE"] = "1"; //全一致
        }

        if ($this->cmd == "exec") {
            $ret = knjl064iQuery::executeQuery($this);

            $this->setMessage("MSG202");
        }
    }
}
