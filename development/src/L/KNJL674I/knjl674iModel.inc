<?php

require_once('for_php7.php');

class knjl674iModel extends Model
{
    public $cmd;
    public $field = array();
    public $entexamyear;
    public $applicantdiv;
    public $testdiv;
    public $generalDivHonorcd;
    public $generalDivHonorReason;
    public $selectLeft;
    public $selectLeftText;
    public $selectRight;
    public $selectRightText;

    public function knjl674iModel()
    {
    }

    public function init()
    {
        $this->entexamyear    = CTRL_YEAR + 1;
        $this->applicantdiv   = "2";  // '2'固定
        $this->testdiv        = "0";  // '0'固定
        $this->generalDivHonorcd     = "04"; // '04'：特待コード　固定
        $this->generalDivHonorReason = "05"; // '05'：特待理由　固定

        $this->cmd = VARS::request("cmd");

        $this->field = array("HONORDIV"            => VARS::post("HONORDIV"),
                             "HONOR_REASONDIV"     => VARS::post("HONOR_REASONDIV"),
                             "FINSCHOOLCD"         => VARS::post("FINSCHOOLCD"),
                             "UPD_HONORDIV"        => VARS::post("UPD_HONORDIV"),
                             "UPD_HONOR_REASONDIV" => VARS::post("UPD_HONOR_REASONDIV")
                            );

        $this->selectLeft      = VARS::post("selectLeft");
        $this->selectLeftText  = VARS::post("selectLeftText");
        $this->selectRight     = VARS::post("selectRight");
        $this->selectRightText = VARS::post("selectRightText");
    }

    //入力項目チェック
    public function validateInput()
    {
        //データ指定なし
        if (!$this->selectLeft) {
            $this->setWarning("MSG304");
            return false;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            $result1 = knjl674iQuery::getUpdateQuery($this, "left");  //左側リスト
            if ($result1) {
                $this->setMessage("MSG201");
                $this->field["HONORDIV"] = $this->field["UPD_HONORDIV"];
                $this->field["HONOR_REASONDIV"] = $this->field["UPD_HONOR_REASONDIV"];
            }
        }
    }
}
