<?php

require_once('for_php7.php');

class knjd131hModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjd131hModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                            );

        $schregno_array = explode(',',$this->data["SCHREGNO"]);
        foreach($schregno_array as $key => $schregno) {
            $this->data["SPECIALACTREMARK".$schregno]   = VARS::post("SPECIALACTREMARK".$schregno);
            $this->data["TOTALSTUDYTIME".$schregno]     = VARS::post("TOTALSTUDYTIME".$schregno);
            $this->data["COMMUNICATION1".$schregno]     = VARS::post("COMMUNICATION1".$schregno);
            $this->data["COMMUNICATION2".$schregno]     = VARS::post("COMMUNICATION2".$schregno);
            $this->data["COMMUNICATION3".$schregno]     = VARS::post("COMMUNICATION3".$schregno);
        }

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
    }
    
    //入力項目チェック
    function validateInput($schregno, $specialactremark, $totalstudytime, $communication1, $communication2, $communication3) {
        if ($schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($specialactremark, (10 * 2)) > 10) {
            $this->setWarning("MSG915","(特別活動の記録は10行までです(学籍番号：$schregno))");
            return false;
        }
        
        return true;
    }


    //更新用のモデルを返します
    function &getUpdateModel() {

        $db = Query::dbCheckOut();

        $schregno_array = explode(',',$this->data["SCHREGNO"]);
        foreach($schregno_array as $key => $schregno) {
            //更新
            if ($this->validateInput($schregno, $this->data["SPECIALACTREMARK".$schregno], $this->data["TOTALSTUDYTIME".$schregno], $this->data["COMMUNICATION1".$schregno], $this->data["COMMUNICATION2".$schregno], $this->data["COMMUNICATION3".$schregno])) {
                knjd131hQuery::getDetailUpdateQuery($db, $this, $schregno, $this->data["SPECIALACTREMARK".$schregno], $this->data["TOTALSTUDYTIME".$schregno], $this->data["COMMUNICATION1".$schregno], $this->data["COMMUNICATION2".$schregno], $this->data["COMMUNICATION3".$schregno]);
            }
        }

        $this->cmd = "knjd131h";
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
}
?>
