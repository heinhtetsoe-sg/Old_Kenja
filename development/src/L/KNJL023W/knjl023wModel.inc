<?php

require_once('for_php7.php');

class knjl023wModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受付番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl023wModel()
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
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;

        //フィールド
        $this->field = array("KESSEKI"          =>  VARS::post("KESSEKI"),        //欠席
                             "GANSHO_HENKOU"    =>  VARS::post("GANSHO_HENKOU")   //願変
                            );
    }

    //入力項目チェック
    function validateInput() {
        if (!strlen($this->examno)) {
            $this->setWarning("MSG301", "( 受付番号 )");
            return false;
        }
        if (strlen($this->field["KESSEKI"]) && strlen($this->field["GANSHO_HENKOU"])) {
            $this->setWarning("MSG901", "両方はチェックできません。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl023wQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>