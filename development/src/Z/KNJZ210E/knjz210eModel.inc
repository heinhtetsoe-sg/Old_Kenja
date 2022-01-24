<?php

require_once('for_php7.php');

class knjz210eModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    function knjz210eModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        if (VARS::get("SEND_PRGRID")) {
            $this->getPrgId = VARS::get("SEND_PRGRID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            
            $this->field["DIV"]             = VARS::get("SEND_DIV");
            $this->field["GRADE"]           = VARS::get("SEND_GRADE");
            $this->field["CLASSCD"]         = VARS::get("SEND_CLASSCD");
            $this->field["SUBCLASSCD"]      = VARS::get("SEND_SUBCLASSCD");
            unset($this->field["VIEWCD"]);
            unset($this->field["MAX_ASSESSLEVEL"]);
        }
        //子画面コンボ変更時
        if (VARS::request("cmd") == "main" || VARS::request("cmd") == "kakutei") {
            $this->field = array("DIV"              =>  VARS::post("DIV"),              //区分
                                 "GRADE"            =>  VARS::post("GRADE"),            //学年
                                 "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                                 "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                                 "VIEWCD"           =>  VARS::post("VIEWCD"),           //観点
                                 "MAX_ASSESSLEVEL"  =>  VARS::post("MAX_ASSESSLEVEL"),  //段階数
                                 "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                                );
        }
        
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["NAMESPARE2"]); $i++) { //NAMESPARE2は段階値数
            //段階値
            $assesslevel  = "ASSESSLEVEL"."-".$i;
            $this->fields["ASSESSLEVEL"][]  = VARS::post($assesslevel);
            //下限値
            $assesslow  = "ASSESSLOW"."-".$i;
            $this->fields["ASSESSLOW"][]  = VARS::post($assesslow);
            //記号
            $assessmark  = "ASSESSMARK"."-".$i;
            $this->fields["ASSESSMARK"][]  = VARS::post($assessmark);
            //段階値の上限値をhiddenで保持
            if ($i > 0) {
                $this->fields["ASSESSHIGHT"][get_count($this->data["NAMESPARE2"]) - $i] = VARS::request("Assesshightvalue".$i);
            }
        }

        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd     = VARS::request("cmd");
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            if (VARS::get("SEND_selectSchoolKind")) {
                $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
            }
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["GRADE"] == "") {
            $this->setWarning("MSG304", "( 学年 )");
            return false;
        }

        if ($this->field["SUBCLASSCD"] == "") {
            $this->setWarning("MSG304", "( 科目 )");
            return false;
        }
        
        if ($this->field["DIV"] === '1') {
            if ($this->field["VIEWCD"] == "") {
                $this->setWarning("MSG304", "( 観点 )");
                return false;
            }
        }

        //段階値得点の前後チェック
        for ($i = 0; $i < get_count($this->data["NAMESPARE2"]); $i++) {
            //段階値チェック
            if ($this->fields["ASSESSLEVEL"][$i] == "") {
                $this->setWarning("MSG901","(段階値が未入力の箇所があります。)");
                return false;
            }
            if ($this->fields["ASSESSLEVEL"][$i] < $this->fields["ASSESSLEVEL"][$i+1]) {
                $this->setWarning("MSG901","(段階値は上から降順に設定して下さい。)");
                return false;
            }
            //重複チェック
            if ($i == 0) {
                for ($k = 1; $k < get_count($this->data["NAMESPARE2"]); $k++) {
                    if ($this->fields["ASSESSLEVEL"][$i] == $this->fields["ASSESSLEVEL"][$k]) {
                        $this->setWarning("MSG901","(段階値が重複しています。)");
                        return false;
                    }
                }
            }
            //記号チェック
            if (strlen($this->fields["ASSESSMARK"][$i]) > 6) {
                $this->setWarning("MSG901","(記号は6バイトまでです)");
                return false;
            }
            //範囲チェック
            if (($this->fields["ASSESSLEVEL"][0] * $this->viewflg_check) < $this->fields["ASSESSLOW"][0]) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->fields["ASSESSLOW"][$i] < $this->fields["ASSESSLOW"][$i+1]) {
                $this->setWarning("MSG901","(段階値の下限値の範囲が不正です)");
                return false;
            } else if ($i == 0 && (($this->fields["ASSESSLEVEL"][0] * $this->viewflg_check) < $this->fields["ASSESSLOW"][1])) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($i > 0 && ($this->fields["ASSESSHIGHT"][$i] < $this->fields["ASSESSLOW"][$i])) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->fields["ASSESSHIGHT"][get_count($this->data["NAMESPARE2"]) - 1] <= "0") {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->fields["ASSESSLOW"][0] <= $this->fields["ASSESSLOW"][1]) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } 
            if ($this->fields["ASSESSLOW"][0] == "" || $this->fields["ASSESSLOW"][1] == "") {
                $this->setWarning("MSG901","(段階値の下限値を入力しなおしてください)");
                return false;
            } else if (($i > 0 && $this->fields["ASSESSHIGHT"][$i] == "") || ($i != (get_count($this->data["NAMESPARE2"]) - 1) && $this->fields["ASSESSLOW"][$i] == "")) {
                $this->setWarning("MSG901","(段階値の下限値を入力しなおしてください)");
                return false;
            }            
        }

        return true;
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjz210eQuery::delete($this, $db);
            knjz210eQuery::update($this, $db);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
        return true;
    }
}
//var_dump($_REQUEST);
?>
