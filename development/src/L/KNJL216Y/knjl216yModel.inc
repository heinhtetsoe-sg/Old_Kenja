<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl216yModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $judgment_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl216yModel()
    {
    }
    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->seq);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
            unset($this->seq);
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");
        $this->ObjYear = CTRL_YEAR + 1;
        $this->semester = CTRL_SEMESTER;

        if (VARS::get("SEQ") != "") {
            $this->seq  = VARS::get("SEQ");
        }

        $this->field = array("NAME"         =>  VARS::post("NAME"),
                             "AGE"          =>  VARS::post("AGE"),
                             "RELATIONSHIP" =>  VARS::post("RELATIONSHIP"),
                             "REMARK"       =>  VARS::post("REMARK")
                     );

        $this->cmd         = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->cmd == "insert" || $this->cmd == "update") {
            if (strlen($this->field["NAME"]) == 0) {
                $this->setWarning("MSG301","氏名");
                return false;
            }
            if (strlen($this->field["RELATIONSHIP"]) == 0) {
                $this->setWarning("MSG301","続柄");
                return false;
            }
            if (strlen($this->field["NAME"]) > 60) {
                $this->setWarning("MSG915","氏名は60バイトまでです。");
                return false;
            }
            if (strlen($this->field["REMARK"]) > 60) {
                $this->setWarning("MSG915","備考は60バイトまでです。");
                return false;
            }
        }
        if ($this->cmd == "update") {
            if (!$this->seq) {
                $this->setWarning("MSG304","更新するデータを指定して下さい。");
                return false;
            }
        }
        if ($this->cmd == "delete") {
            if (!$this->seq) {
                $this->setWarning("MSG304","削除するデータを指定して下さい。");
                return false;
            }
        }
        return true;
    }

    //新規処理
    function &getInsertModel()
    {
        if ($this->validateInput()) {
            $result = knjl216yQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl216yQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        if ($this->validateInput()) {
            $result = knjl216yQuery::getDeleteQuery($this);
        }
        return;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
