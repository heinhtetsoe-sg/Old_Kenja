<?php

require_once('for_php7.php');

class knjl676iModel extends Model
{
    public $cmd;
    public $field = array();
    public $entexamyear;
    public $applicantdiv;
    public $testdiv;
    public $selectLeft;
    public $selectLeftText;
    public $selectRight;
    public $selectRightText;

    public function knjl676iModel()
    {
    }

    public function init()
    {
        $this->entexamyear    = CTRL_YEAR + 1;
        $this->applicantdiv   = "2";  // '2'固定
        $this->testdiv        = "0";  // '0'固定

        $this->cmd = VARS::request("cmd");

        $this->field = array("PRISCHOOLCD"         => VARS::post("PRISCHOOLCD"),
                             "FINSCHOOLCD"      => VARS::post("FINSCHOOLCD")
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
        if (!$this->selectLeft && !$this->selectRight) {
            $this->setWarning("MSG304");
            return false;
        }

        //塾コード存在チェック
        $db = Query::dbCheckOut();
        $priSchRow = $db->getRow(knjl676iQuery::getPriSchoolRow($this->field["PRISCHOOLCD"]), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($priSchRow)) {
            $this->setWarning("MSG901", "塾コード");
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
            $result1 = knjl676iQuery::getUpdateQuery($this, "left");  //左側リスト
            $result2 = knjl676iQuery::getUpdateQuery($this, "right"); //右側リスト
            if ($result1 || $result2) {
                $this->setMessage("MSG201");
            }
        }
    }
}
