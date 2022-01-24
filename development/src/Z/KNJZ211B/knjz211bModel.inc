<?php

require_once('for_php7.php');

//定数定義
class knjz211bModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $subclasscd;
    var $subclassname;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    //メンバ変数の初期化
    function knjz211bModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->subclasscd = "";
            $this->subclassname = "";
            $this->grade = "";
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
            $this->field["MAX_ASSESSLEVEL"] = VARS::get("SEND_MAX_ASSESSLEVEL");
            unset($this->field["MAX_ASSESSLEVEL"]);
        }
        //子画面コンボ変更時
        if (VARS::request("cmd") == "list" || VARS::request("cmd") == "kakutei") {
            $this->field = array("DIV"              =>  VARS::post("DIV"),              //区分
                                 "GRADE"            =>  VARS::post("GRADE"),            //学年
                                 "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                                 "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                                 "MAX_ASSESSLEVEL"  =>  VARS::post("MAX_ASSESSLEVEL"),  //段階数
                                 "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                                );
        }
        if (VARS::request("cmd") == "kakutei") {
            if ($this->field["GRADE"] == "" || $this->field["SUBCLASSCD"] == "") {
                $this->field["GRADE"] =  $this->grade; //段階数
                $this->field["SUBCLASSCD"] =  $this->subclasscd; //科目コード
            }
        }

        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["NAMESPARE2"]); $i++) { //NAMESPARE2は段階値数
            //段階値
            $assesslevel  = "ASSESSLEVEL"."-".$i;
            $this->fields["ASSESSLEVEL"][]  = VARS::post($assesslevel);
            //下限値
            $assesslow  = "ASSESSLOW"."-".$i;
            $this->fields["ASSESSLOW"][]  = VARS::post($assesslow);
            //段階値の上限値をhiddenで保持
            if ($i > 0) {
                $this->fields["ASSESSHIGHT"][get_count($this->data["NAMESPARE2"]) - $i] = VARS::request("Assesshightvalue".$i);
            }
        }

        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd             = VARS::request("cmd");
                
        //リンク用から取得した科目
        if (VARS::get("SUBCLASSCD")!="") {
            $this->subclasscd    = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("SUBCLASSNAME")!="") {
            $this->subclassname    = VARS::get("SUBCLASSNAME");
        }
        if (VARS::get("GRADE")!="") {
            $this->grade           = VARS::get("GRADE");
        }
        //プロパティファイル
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["GRADE"] == "") {
            $this->setWarning("MSG304", "( 学年 )");
            return false;
        }
        if ($this->subclasscd == "") {
            $this->setWarning("MSG304", "( 科目 )");
            return false;
        }
        if ($this->grade == "" || $this->subclasscd == "") {
            $this->setWarning("MSG304");
            return false;
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
            //範囲チェック
            if ($this->max_assesshight < $this->fields["ASSESSLOW"][0]) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->fields["ASSESSLOW"][$i] < $this->fields["ASSESSLOW"][$i+1]) {
                $this->setWarning("MSG901","(段階値の下限値の範囲が不正です)");
                return false;
            } else if ($i == 0 && ($this->max_assesshight < $this->fields["ASSESSLOW"][1])) {
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
            knjz211bQuery::delete($this, $db);
            knjz211bQuery::update($this, $db);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
        return true;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
