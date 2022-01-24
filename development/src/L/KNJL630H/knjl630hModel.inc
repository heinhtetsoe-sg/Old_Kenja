<?php
class knjl630hModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $sort;
    public $startHuban;
    public $examnoArray;
    public $receptnoArray;

    public function knjl630hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->sort);
        }

        $this->cmd = VARS::request("cmd");
        $this->objYear = CTRL_YEAR + 1;
        $this->applicantdiv = 2;
        $this->testdiv = VARS::post("TESTDIV");
        $this->sort = VARS::post("SORT");
        $this->startHuban = VARS::post("START_HUBAN");
        if ($this->startHuban != "") {
            $this->startHuban = sprintf("%04d", $this->startHuban);
        }
        $this->receptnoArray = array();
    }

    //入力項目チェック
    public function validateInput()
    {
        $returnFlg = true;
        $msg = "";

        foreach ($this->examnoArray as $key => $examno) {
            $postReceptno = VARS::post("RECEPTNO_{$examno}");
            if ($postReceptno != "") {
                $postReceptno = sprintf("%04d", $postReceptno);
            }

            //必須項目チェック
            if (!strlen(trim($postReceptno))) {
                $returnFlg = false;
                $msg .= "\\n受験番号が入力されていません。 ( 登録番号:{$examno} )";
            }

            //重複チェック
            if (knjl630hQuery::isExistReceptno($this, $examno, $postReceptno)) {
                $returnFlg = false;
                $msg .= "\\n受験番号「{$postReceptno}」が重複しています。 ( 登録番号:{$examno} )";
            }

            $this->receptnoArray[$examno]= $postReceptno;
        }

        if (!$returnFlg) {
            $this->setWarning("MSG203", $msg);
        }

        return $returnFlg;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl630hQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
