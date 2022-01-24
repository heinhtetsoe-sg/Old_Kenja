<?php
class knjl740hModel extends Model
{
    public $cmd;
    public $field = array();
    public $ObjYear;
    public $selectLeft;
    public $selectLeftText;
    public $selectRight;
    public $selectRightText;

    public function knjl740hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     => VARS::post("APPLICANTDIV"),
                             "TESTDIV"          => VARS::post("TESTDIV"),
                             "EXAM_TYPE"        => "1",
                             "EXAMHALLCD"       => VARS::post("EXAMHALLCD")
                            );

        $this->selectLeft      = VARS::post("selectLeft");
        $this->selectLeftText  = VARS::post("selectLeftText");
        $this->selectRight     = VARS::post("selectRight");
        $this->selectRightText = VARS::post("selectRightText");
    }

    //入力項目チェック
    public function validateInput($db)
    {
        //データ指定なし
        if (!$this->selectLeft && !$this->selectRight) {
            $this->setWarning("MSG304");
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
            $result1 = knjl740hQuery::getUpdateQuery($this, "left");  //左側リスト
            $result2 = knjl740hQuery::getUpdateQuery($this, "right"); //右側リスト
            if ($result1 || $result2) {
                $this->setMessage("MSG201", $prtStr);
            }
        }
    }
}
