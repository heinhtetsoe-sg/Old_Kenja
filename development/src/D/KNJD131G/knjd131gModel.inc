<?php

require_once('for_php7.php');

class knjd131gModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjd131gModel() {
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
        if ($this->validate_row_cnt($totalstudytime, (10 * 2)) > 10) {
            $this->setWarning("MSG915","(総合的な学習の時間は10行までです(学籍番号：$schregno))");
            return false;
        }
        if ($this->semsterCount == "3" ) {
            if ($this->validate_row_cnt($communication1, (35 * 2)) > 3) {
                $this->setWarning("MSG915","( 通信欄（1学期）は 3行までです(学籍番号：$schregno))");
                return false;
            }
            if ($this->validate_row_cnt($communication2, (35 * 2)) > 3) {
                $this->setWarning("MSG915","( 通信欄（2学期）は 3行までです(学籍番号：$schregno))");
                return false;
            }
            if ($this->validate_row_cnt($communication3, (35 * 2)) > 3) {
                $this->setWarning("MSG915","( 通信欄（3学期）は 3行までです(学籍番号：$schregno))");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($communication1, (35 * 2)) > 3) {
                $this->setWarning("MSG915","( 通信欄（前期）は 3行までです(学籍番号：$schregno))");
                return false;
            }
            if ($this->validate_row_cnt($communication2, (35 * 2)) > 3) {
                $this->setWarning("MSG915","( 通信欄（後期）は 3行までです(学籍番号：$schregno))");
                return false;
            }
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
                knjd131gQuery::getDetailUpdateQuery($db, $this, $schregno, $this->data["SPECIALACTREMARK".$schregno], $this->data["TOTALSTUDYTIME".$schregno], $this->data["COMMUNICATION1".$schregno], $this->data["COMMUNICATION2".$schregno], $this->data["COMMUNICATION3".$schregno]);
            }
        }

        $this->cmd = "knjd131g";
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
}
?>
