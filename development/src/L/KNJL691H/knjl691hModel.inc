<?php
class knjl691hModel extends model
{
    public $cmd;
    public $objYear;
    public $applicantdiv;
    public $startHuban;
    public $examnoArray;
    public $receptnoArray;
    public $studentnoArray;

    public function knjl691hModel()
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
    }

    //入力項目チェック
    public function validateInput()
    {
        $returnFlg = true;
        $msg = "";

        foreach ($this->receptnoArray as $key => $receptno) {
            $postStudentno = VARS::post("STUDENTNO_{$receptno}");
            if ($postStudentno != "") {
                //入力されている場合、8桁ゼロ埋め
                $postStudentno = sprintf("%08d", $postStudentno);
            }

            //必須項目チェック
            if (!strlen(trim($postStudentno))) {
                $returnFlg = false;
                $msg .= "\\n学籍番号が入力されていません。 ( 受験番号:{$receptno} )";
            }

            // //重複チェック
            // if (knjl691hQuery::isExistStudentno($this, $examno, $postStudentno)) {
            //     $returnFlg = false;
            //     $msg .= "\\n学籍番号が重複しています。 ( 受験番号:{$examno} )";
            // }

            $this->studentnoArray[$receptno] = $postStudentno;
            $this->examnoArray[$receptno] = VARS::post("EXAMNO_{$receptno}");
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
            knjl691hQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
