<?php

require_once('for_php7.php');

class knjf305Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    function knjf305Model() {}

    function init() {
        common::GetControlMaster_Fnc($this->control);

        if (VARS::get("PROGRAMID") != "") {
            unset($this->data_div);
        }

        $this->data_div = VARS::post("DATA_DIV");

        $this->field = array("YEAR"                   =>  VARS::post("YEAR"),                 //年度
                             "DISEASECD"              =>  VARS::post("DISEASECD"),            //理由（疾患病）
                             "GRADE"                  =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"               =>  VARS::post("HR_CLASS"),             //組
                             "PATIENT_COUNT"          =>  VARS::post("PATIENT_COUNT"),        //出席停止者数
                             "ABSENCE_COUNT"          =>  VARS::post("ABSENCE_COUNT"),        //罹患欠席者数
                             "PRESENCE_COUNT"         =>  VARS::post("PRESENCE_COUNT"),       //罹患出席者数
                             "ACTION_S_DATE"          =>  VARS::post("ACTION_S_DATE"),        //措置期間自
                             "ACTION_E_DATE"          =>  VARS::post("ACTION_E_DATE"),        //措置期間至
                             "SYMPTOM01"              =>  VARS::post("SYMPTOM01"),            //症状01
                             "SYMPTOM01_REMARK"       =>  VARS::post("SYMPTOM01_REMARK"),     //症状01備考
                             "SYMPTOM02"              =>  VARS::post("SYMPTOM02"),            //症状02
                             "SYMPTOM03"              =>  VARS::post("SYMPTOM03"),            //症状03
                             "SYMPTOM04"              =>  VARS::post("SYMPTOM04"),            //症状04
                             "SYMPTOM05"              =>  VARS::post("SYMPTOM05"),            //症状05
                             "SYMPTOM06"              =>  VARS::post("SYMPTOM06"),            //症状06
                             "SYMPTOM07"              =>  VARS::post("SYMPTOM07"),            //症状07
                             "SYMPTOM08"              =>  VARS::post("SYMPTOM08"),            //症状08
                             "SYMPTOM09"              =>  VARS::post("SYMPTOM09"),            //症状09
                             "SYMPTOM10"              =>  VARS::post("SYMPTOM10"),            //症状10
                             "SYMPTOM11"              =>  VARS::post("SYMPTOM11"),            //症状11
                             "SYMPTOM12"              =>  VARS::post("SYMPTOM12"),            //症状12
                             "SYMPTOM12_REMARK"       =>  VARS::post("SYMPTOM12_REMARK"),     //症状12備考
                             "REMARK"                 =>  VARS::post("REMARK"),               //備考
                             
                             "HEISA_DIV"              =>  VARS::post("HEISA_DIV"),            //閉鎖区分（データ保持用）
                             "EXECUTE_DATE"           =>  VARS::post("EXECUTE_DATE"),         //作成日（報告履歴テーブル）
                             "DBNAME"                 =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        if ($this->data_div === '0_new' && $this->cmd === 'change') {
            unset($this->field);
            $this->field = array("YEAR"                   =>  VARS::post("YEAR"),                 //年度
                                 
                                 "HEISA_DIV"              =>  VARS::post("HEISA_DIV"),            //閉鎖区分（データ保持用）
                                 "EXECUTE_DATE"           =>  VARS::post("EXECUTE_DATE"),         //作成日（報告履歴テーブル）
                                 "DBNAME"                 =>  VARS::post("DBNAME")                //DB変更に備えて
                                );
        } else if ($this->data_div !== '0_new' && $this->cmd === 'change') {
            $data_div = explode(":", $this->data_div);
            $this->field["ACTION_S_DATE"]   = str_replace("-","/",$data_div[0]);
            $this->field["GRADE"]           = $data_div[1];
            $this->field["HR_CLASS"]        = $data_div[2];
            $this->field["DISEASECD"]       = $data_div[3];
            if ($this->field["GRADE"] == '99' && $this->field["HR_CLASS"] == '999') {
                $this->field["HEISA_DIV"]   = "3";
            } else if ($this->field["GRADE"] != '99' && $this->field["HR_CLASS"] == '999') {
                $this->field["HEISA_DIV"]   = "2";
            } else {
                $this->field["HEISA_DIV"]   = "1";
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {

        return true;
    }

    function &getUpdateEdboardModel() {
        if ($this->validateInput()) {
            //閉鎖区分により学年、クラスの値をセット
            if ($this->field["HEISA_DIV"] === '2' || $this->field["HEISA_DIV"] === '3') {
                $this->field["HR_CLASS"] = '999';
            }
            if ($this->field["HEISA_DIV"] === '3') {
                $this->field["GRADE"] = '99';
            }
        
            //DELETE 県側テーブルMEDEXAM_DISEASE_ADDITION4_DAT, 学校側報告テーブルREPORT_DISEASE_ADDITION4_DAT
            knjf305Query::DeleteAddtion4Dat($this);
        
            //県側MEDEXAM_DISEASE_ADDITION4_DAT追加 + 学校側報告テーブル（REPORT_DISEASE_ADDITION3_DAT）追加
            knjf305Query::setAddtion4Dat($this);
            
            $this->setMessage("MSG201");
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjf305Query::updateDat($this);
            $this->setMessage("MSG201");

            $this->data_div = str_replace("/","-",$this->field["ACTION_S_DATE"]).':'.$this->field["GRADE"].':'.$this->field["HR_CLASS"].':'.$this->field["DISEASECD"];
        }
        return true;
    }
}
//var_dump($_REQUEST);
?>
