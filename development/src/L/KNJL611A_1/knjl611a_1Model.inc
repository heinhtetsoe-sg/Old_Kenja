<?php

require_once('for_php7.php');

class knjl611a_1Model extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl611a_1Model()
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
            $this->testdiv      = VARS::get("SEND_TESTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;

        //フィールド
        $this->field = array("TOTAL5"           =>  VARS::post("TOTAL5"),           //5科評定合計
                             "ABSENCE_DAYS3"    =>  VARS::post("ABSENCE_DAYS3"),    //欠席日数
                             "SPECIALACTREC"    =>  VARS::post("SPECIALACTREC"),    //生徒会活動/クラス活動
                             "TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")    //特別活動推薦
                            );
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl611a_1Query::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>