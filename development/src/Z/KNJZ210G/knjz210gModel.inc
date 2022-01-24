<?php

require_once('for_php7.php');

//定数定義
class knjz210gModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $subclasscd;
    var $studyrec_viewcd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    
    //メンバ変数の初期化
    function knjz210gModel()
    {
    }
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->subclasscd = "";
            $this->studyrec_viewcd = "";
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        if (VARS::get("SEND_GRADE") != "") {
            $this->getGrade = VARS::get("SEND_GRADE");
        }

        if (VARS::get("SEND_PRGRID")) {
            $this->getPrgId = VARS::get("SEND_PRGRID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            
            $this->field["DIV"]             = VARS::get("SEND_DIV");
            $this->field["GRADE"]           = VARS::get("SEND_GRADE");
            $this->field["CLASSCD"]         = VARS::get("SEND_CLASSCD");
            $this->field["SUBCLASSCD"]      = VARS::get("SEND_SUBCLASSCD");
        }
        //子画面コンボ変更時
        if (VARS::request("cmd") == "list") {
            $this->field = array("DIV"              =>  VARS::post("DIV"),              //区分
                                 "GRADE"            =>  VARS::post("GRADE"),            //学年
                                 "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                                 "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                                 "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                                );
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //段階値を配列で取得
        $result = $db->query(knjz210gQuery::getNamespare2($this, 'D029'));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->data["NAMESPARE2"][] = $row["NAMESPARE2"];
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);
        
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["NAMESPARE2"]); $i++) { //NAMESPARE2は段階値
            $assesslow  = "ASSESSLOW"."-".$i;
            $this->fields["ASSESSLOW"][]  = VARS::post($assesslow);
        }
        $this->Assesshightvalue2 = VARS::post("Assesshightvalue2");
        $this->Assesshightvalue1 = VARS::post("Assesshightvalue1");
        
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd             = VARS::request("cmd");
                
        //リンク用から取得した科目
        if (VARS::get("STUDYREC_SUBCLASSCD")!="") {
            $this->subclasscd    = VARS::get("STUDYREC_SUBCLASSCD");
        }
        if (VARS::get("STUDYREC_VIEWCD")!="") {
            $this->studyrec_viewcd    = VARS::get("STUDYREC_VIEWCD");
        }
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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

        //段階値得点の前後チェック
        for ($i = 0; $i < get_count($this->data["NAMESPARE2"]); $i++) {
            if ($this->data["ASSESSHIGH"][0] < $this->fields["ASSESSLOW"][0]) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->Assesshightvalue2 < $this->fields["ASSESSLOW"][1]) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->Assesshightvalue1 < $this->fields["ASSESSLOW"][2]) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->Assesshightvalue1 <= "0") {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } else if ($this->fields["ASSESSLOW"][0] <= $this->fields["ASSESSLOW"][1]) {
                $this->setWarning("MSG901","(段階値の下限値と上限値の範囲が不正です)");
                return false;
            } 
            if ($this->fields["ASSESSLOW"][0] == "" || $this->fields["ASSESSLOW"][1] == "") {
                $this->setWarning("MSG901","(段階値の下限値を入力しなおしてください)");
                return false;
            } else if ($this->Assesshightvalue1 == "" || $this->Assesshightvalue2 == "") {
                $this->setWarning("MSG901","(段階値の下限値を入力しなおしてください)");
                return false;
            }
        }

        return true;
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjz210gQuery::delete($this, $db);
            knjz210gQuery::update($this, $db);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);
        return true;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
