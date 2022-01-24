<?php

require_once('for_php7.php');

class knjl021fModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $field = array();   //フィールドデータ

    function knjl021fModel()
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
        $this->field = array("CONFIDENTIAL_RPT01"   =>  VARS::post("CONFIDENTIAL_RPT01"),       //国語
                             "CONFIDENTIAL_RPT02"   =>  VARS::post("CONFIDENTIAL_RPT02"),       //社会
                             "CONFIDENTIAL_RPT03"   =>  VARS::post("CONFIDENTIAL_RPT03"),       //数学
                             "CONFIDENTIAL_RPT04"   =>  VARS::post("CONFIDENTIAL_RPT04"),       //理科
                             "CONFIDENTIAL_RPT05"   =>  VARS::post("CONFIDENTIAL_RPT05"),       //音楽
                             "CONFIDENTIAL_RPT06"   =>  VARS::post("CONFIDENTIAL_RPT06"),       //美術
                             "CONFIDENTIAL_RPT07"   =>  VARS::post("CONFIDENTIAL_RPT07"),       //保険
                             "CONFIDENTIAL_RPT08"   =>  VARS::post("CONFIDENTIAL_RPT08"),       //技術
                             "CONFIDENTIAL_RPT09"   =>  VARS::post("CONFIDENTIAL_RPT09"),       //外国
                             "CONFIDENTIAL_RPT10"   =>  VARS::post("CONFIDENTIAL_RPT10"),       //未使用
                             "TOTAL3"               =>  VARS::post("TOTAL3"),                   //3科計
                             "TOTAL5"               =>  VARS::post("TOTAL5"),                   //5科計
                             "TOTAL_ALL"            =>  VARS::post("TOTAL_ALL"),                //合計
                             "KASANTEN_ALL"         =>  VARS::post("KASANTEN_ALL"),             //段階
                             //備考２（活動評価点）
                             "REMARK1"              =>  VARS::post("REMARK1")                   //特活・部活・特記事項要約
                            );
    }

    //入力項目チェック
    function validateInput()
    {
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915","備考２は60バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl021fQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

}
?>