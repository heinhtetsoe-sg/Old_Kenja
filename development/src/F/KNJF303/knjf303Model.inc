<?php

require_once('for_php7.php');

class knjf303Model extends Model {
    var $field = array();
    var $data  = array();
    var $year;
    var $suspend_direct_date;
    var $data_div;
    var $cmd;
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    function knjf303Model() {}

    function init() {
        common::GetControlMaster_Fnc($this->control);

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->suspend_direct_date);
            unset($this->data_div);
        }

        $this->year = VARS::post("YEAR");
        $this->suspend_direct_date = VARS::post("SUSPEND_DIRECT_DATE");
        $this->data_div = VARS::post("DATA_DIV");

        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SUSPEND_DIRECT_DATE"  =>  VARS::post("SUSPEND_DIRECT_DATE"),  //出席停止を指示した日
                             "TOTAL_DIV"            =>  VARS::post("TOTAL_DIV"),            //集計区分
                             "DISEASECD"            =>  VARS::post("DISEASECD"),            //理由（疾患病）
                             "DISEASECD_REMARK"     =>  VARS::post("DISEASECD_REMARK"),     //その他の疾患病
                             "SUSPEND_S_DATE"       =>  VARS::post("SUSPEND_S_DATE"),       //出席停止開始日
                             "SUSPEND_E_DATE"       =>  VARS::post("SUSPEND_E_DATE"),       //出席停止終了日
                             "SUSPEND_REMARK"       =>  VARS::post("SUSPEND_REMARK"),       //備考
                             "REMARK1"              =>  VARS::post("REMARK1"),              //備考1
                             "REMARK2"              =>  VARS::post("REMARK2"),              //備考2
                             "REMARK3"              =>  VARS::post("REMARK3"),              //備考3
                             "DATA_DIV"             =>  VARS::post("DATA_DIV"),             //履歴を保持するフィールド
                             
                             "EXECUTE_DATE"         =>  VARS::post("EXECUTE_DATE"),         //作成日（報告履歴テーブル）
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );
        
        if (VARS::get("SEND_PRGRID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            common::GetControlMaster_Fnc($this->control);
            
            $this->getPrgId = VARS::get("SEND_PRGRID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->year                = VARS::get("SEND_YEAR");
            $this->data_div            = VARS::get("SEND_DATA_DIV");

            list($this->suspend_direct_date, $this->field["TOTAL_DIV"], $this->field["DISEASECD"]) = explode(':', $this->data_div);

        }
        
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd     = VARS::request("cmd");
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();

    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        //データの確認
        $row = $db->getRow(knjf303Query::countAddition3Dat($this, "DATA"), DB_FETCHMODE_ASSOC);

        if ($this->cmd == "update"){
            if ($this->data_div == '0_new' && is_array($row)){
                $this->setWarning("MSG302", $row["NAME1"]."での出席停止届けは、既に作成されています。");
                return false;
            }
            if ($this->field["TOTAL_DIV"] == "") {
                $this->setWarning("MSG301","(集計区分)");
                return false;
            }
            if (strlen($this->field["DISEASECD_REMARK"]) > 150) {
                $this->setWarning("MSG915","(その他の疾患病は150バイトまでです)");
                return false;
            }
            if (strlen($this->field["SUSPEND_REMARK"]) > 150) {
                $this->setWarning("MSG915","(理由備考は150バイトまでです)");
                return false;
            }
            if (strlen($this->field["REMARK1"]) > 150) {
                $this->setWarning("MSG915","(備考1(学校医の意見)は150バイトまでです)");
                return false;
            }
            if (strlen($this->field["REMARK2"]) > 150) {
                $this->setWarning("MSG915","(備考2(今後の処置)は150バイトまでです)");
                return false;
            }
            if (strlen($this->field["REMARK3"]) > 150) {
                $this->setWarning("MSG915","(備考3(その他)は150バイトまでです)");
                return false;
            }
        }
        
        if ($this->cmd == "houkoku"){
            //必須項目チェック
            if ($this->field["TOTAL_DIV"] == "") {
                $this->setWarning("MSG301","(集計区分)");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    function &getUpdateEdboardModel() {
        if ($this->validateInput()) {
            //DELETE 県側テーブルMEDEXAM_DISEASE_ADDITION3_DAT, MEDEXAM_DISEASE_ADDITION3_COUNT_DAT, 学校側REPORT_DISEASE_ADDITION3_DAT
            knjf303Query::DeleteAddtion3Dat($this);
        
            //県側MEDEXAM_DISEASE_ADDITION3_DAT追加
            knjf303Query::setAddtion3Dat($this);
            
            //県側MEDEXAM_DISEASE_ADDITION3_COUNT_DAT追加 + 学校側報告テーブル（REPORT_DISEASE_ADDITION3_DAT）追加
            knjf303Query::setAddtion3Dat2($this);
            
            $this->setMessage("MSG201");
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["GRADE_HR_CLASS"]); $i++) {
            $count1 = "TEISHI_COUNT"."-".$i;
            $count2 = "KESSEKI_COUNT"."-".$i;
            $count3 = "TOUKOU_COUNT"."-".$i;
            $this->fields["TEISHI_COUNT"][]     = VARS::post($count1);
            $this->fields["KESSEKI_COUNT"][]    = VARS::post($count2);
            $this->fields["TOUKOU_COUNT"][]     = VARS::post($count3);
        }
        if ($this->validateInput()) {
            knjf303Query::updateDat($this);
            if ($this->field["TOTAL_DIV"] === '1') {
                knjf303Query::updateCountDat($this, "TEISHI");
            } else if ($this->field["TOTAL_DIV"] === '2') {
                knjf303Query::updateCountDat($this, "KESSEKI");
            } else if ($this->field["TOTAL_DIV"] === '3') {
                knjf303Query::updateCountDat($this, "TOUKOU");
            }
            $this->setMessage("MSG201");

            $this->data_div = str_replace("/","-",$this->suspend_direct_date).':'.$this->field["TOTAL_DIV"].':'.$this->field["DISEASECD"];
        }
        return true;
    }
}
//var_dump($_REQUEST);
?>
