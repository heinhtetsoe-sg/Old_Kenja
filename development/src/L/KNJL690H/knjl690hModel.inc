<?php
class knjl690hModel extends Model
{
    public $cmd;
    public $field = array();
    public $ObjYear;

    public function knjl690hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     => "2",
                             "EXAM_TYPE"        => "1",
                             "RECEPTNO"         => VARS::post("RECEPTNO"),
                             "JUDGEMENT"        => VARS::post("JUDGEMENT"),
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
        if ($this->field["RECEPTNO"] == "") {
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
            $result = knjl690hQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201", $prtStr);
            }
        }
    }
}
