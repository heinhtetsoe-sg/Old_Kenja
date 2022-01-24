<?php

require_once('for_php7.php');

class knjl790hModel extends Model
{
    public $cmd;
    public $field = array();
    public $ObjYear;

    public function knjl790hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     => VARS::post("APPLICANTDIV"),
                             "TESTDIV"          => VARS::post("TESTDIV"),
                             "EXAMNO"           => VARS::post("EXAMNO"),
                             "JUDGEMENT"        => VARS::post("JUDGEMENT"),
                             "SUB_ORDER"        => VARS::post("SUB_ORDER"),
                             "UNPASS"           => VARS::post("UNPASS"),
                             "PROCEDUREDIV"     => VARS::post("PROCEDUREDIV"),
                             "PROCEDUREDATE"    => VARS::post("PROCEDUREDATE"),
                             "ENTDIV"           => VARS::post("ENTDIV"),
                             "STUDENTNO"        => VARS::post("STUDENTNO")
                            );
    }

    //入力項目チェック
    public function validateInput($db)
    {

        //必須項目
        if ($this->field["APPLICANTDIV"] == "") {
            $this->setWarning("MSG301", "( 学校種別 )");
            return true;
        }
        if ($this->field["TESTDIV"] == "") {
            $this->setWarning("MSG301", "( 入試区分 )");
            return true;
        }
        if ($this->field["EXAMNO"] == "") {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        return ture;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput($db)) {
            //更新処理
            $result = knjl790hQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201", $prtStr);
            }
        }
    }
}
