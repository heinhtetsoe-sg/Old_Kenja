<?php

require_once('for_php7.php');

class knjl021uModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl021uModel()
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
        $this->field = array("CONFIDENTIAL_RPT01"   =>  VARS::post("CONFIDENTIAL_RPT01"),       //国語
                             "CONFIDENTIAL_RPT02"   =>  VARS::post("CONFIDENTIAL_RPT02"),       //社会
                             "CONFIDENTIAL_RPT03"   =>  VARS::post("CONFIDENTIAL_RPT03"),       //数学
                             "CONFIDENTIAL_RPT04"   =>  VARS::post("CONFIDENTIAL_RPT04"),       //理科
                             "CONFIDENTIAL_RPT05"   =>  VARS::post("CONFIDENTIAL_RPT05"),       //音楽
                             "CONFIDENTIAL_RPT06"   =>  VARS::post("CONFIDENTIAL_RPT06"),       //美術
                             "CONFIDENTIAL_RPT07"   =>  VARS::post("CONFIDENTIAL_RPT07"),       //保険
                             "CONFIDENTIAL_RPT08"   =>  VARS::post("CONFIDENTIAL_RPT08"),       //技術
                             "CONFIDENTIAL_RPT09"   =>  VARS::post("CONFIDENTIAL_RPT09"),       //外国
                             "AVERAGE5"             =>  VARS::post("AVERAGE5"),                 //5科平均(3年)
                             "AVERAGE_ALL"          =>  VARS::post("AVERAGE_ALL"),              //9科平均(3年)
                             //欠席の記録
                             "ABSENCE_DAYS3"        =>  VARS::post("ABSENCE_DAYS3"),            //欠席日数(3年)
                            );
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl021uQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>