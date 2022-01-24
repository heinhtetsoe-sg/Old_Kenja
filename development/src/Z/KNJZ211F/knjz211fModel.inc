<?php

require_once('for_php7.php');

class knjz211fModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjz211fModel() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }

        $this->field = array("DIV"              =>  VARS::post("DIV"),              //区分
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "VIEWCD"           =>  VARS::post("VIEWCD"),           //観点
                             "VIEWCD2"          =>  VARS::post("VIEWCD2"),          //観点参照元
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "MAX_ASSESSLEVEL"  =>  VARS::post("MAX_ASSESSLEVEL"),  //段階数
                             "CLASSCD_ALL_CHECK"=>  VARS::post("CLASSCD_ALL_CHECK"),//全教科も作成するチェック
                             "IS_HUITTI_FLAG"   =>  VARS::post("IS_HUITTI_FLAG"),   //不一致かどうか
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->fields = array();
        $this->fields2 = array();
        for ($i = 0; $i < get_count($this->data["ROW_NUM"]); $i++) { //ROW_NUMは段階値数
            //段階値
            $assesslevel  = "ASSESSLEVEL"."-".$i;
            $this->fields["ASSESSLEVEL"][]  = VARS::post($assesslevel);
            //下限値
            $assesslow = "ASSESSLOW"."-".$i;
            $this->fields["ASSESSLOW"][]  = VARS::post($assesslow);
            $this->fields2["ASSESSLOW"][]  = VARS::post($assesslow);
            //記号
            $assessmark  = "ASSESSMARK"."-".$i;
            $this->fields["ASSESSMARK"][]  = VARS::post($assessmark);

            //段階値の上限値をhiddenで保持
            $this->fields["ASSESSHIGH"][] = VARS::request("Assesshighvalue".(get_count($this->data["ROW_NUM"]) - $i));
            if ($i>0) {
            	$this->fields2["ASSESSHIGH"][get_count($this->data["ROW_NUM"]) - $i] = VARS::request("Assesshighvalue".$i);
            }
        }
        $this->cmd     = VARS::request("cmd");

        //プロパティファイルの読込み
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

		if($this->field['CLASSCD'] != 'all'){
	        if ($this->field["SUBCLASSCD"] == "") {
	            $this->setWarning("MSG304", "( 科目 )");
	            return false;
	        }

	        if ($this->field['DIV'] !='2' && $this->field["VIEWCD"] == "") {
	            $this->setWarning("MSG304", "( 観点 )");
	            return false;
	        }
        }
        if($this->cmd == 'delete'){
        	return true;
        }

        //段階値得点の前後チェック
        for ($i = 0; $i < get_count($this->data["ROW_NUM"]); $i++) {
            //段階値チェック
            if ($this->fields["ASSESSLEVEL"][$i] == "") {
                $this->setWarning("MSG901","(段階値が未入力の箇所があります。)");
                return false;
            }
            if ($this->fields["ASSESSLEVEL"][$i] < $this->fields["ASSESSLEVEL"][$i+1]) {
                $this->setWarning("MSG901","(段階値は上から降順に設定して下さい。)");
                return false;
            }
            //下限値チェック
            if ($this->fields["ASSESSLOW"][$i] == "" && ($i !== get_count($this->data["ROW_NUM"]) -1)) {
                $this->setWarning("MSG901","(下限値が未入力の箇所があります。)");
                return false;
            }
            //重複チェック
            for ($k = 0; $k < get_count($this->data["ROW_NUM"]); $k++) {
                if ($i != $k) {
                    if ($this->fields["ASSESSLEVEL"][$i] == $this->fields["ASSESSLEVEL"][$k]) {
                        $this->setWarning("MSG901","(段階値が重複しています。)");
                        return false;
                    }
                    if ($this->fields["ASSESSLOW"][$i] == $this->fields["ASSESSLOW"][$k]) {
                        $this->setWarning("MSG901","(下限値が重複しています。)");
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
            if ($this->fields["ASSESSLOW"][$i] < $this->fields["ASSESSLOW"][$i+1]) {
                $this->setWarning("MSG901","(下限値の範囲が不正です)");
                return false;
            }
            if (($i > 0 && $this->fields2["ASSESSHIGH"][$i] == "") || ($i != (get_count($this->data["ROW_NUM"]) - 1) && $this->fields2["ASSESSLOW"][$i] == "")) {
                $this->setWarning("MSG901","(下限値を入力しなおしてください)");
                return false;
            }
        }

        return true;
    }

    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjz211fQuery::delete($this, $db);
            if($this->field["DIV"] == 1 && $this->field["IS_HUITTI_FLAG"]==1){
	        	$this->field["DIV"] = 2;
	            knjz211fQuery::delete($this, $db);
	        	$this->field["DIV"] = 1;
        	}
        	
            knjz211fQuery::update($this, $db);
            $this->setMessage("MSG201");

            $this->field["MAX_ASSESSLEVEL"] = get_count($this->data["ROW_NUM"]);
        }
        Query::dbCheckIn($db);
        return true;
    }
    
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjz211fQuery::delete($this, $db);
            $cnt = knjz211fQuery::copy($this, $db);
            $this->setMessage("MSG201");

            $this->field["MAX_ASSESSLEVEL"] = $cnt;
        }
        Query::dbCheckIn($db);
        return true;
    }
    
    function &getUpdateYesModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjz211fQuery::delete_yes($this, $db);
            knjz211fQuery::insert_yes($this, $db);
            $this->setMessage("MSG201");

            $this->field["MAX_ASSESSLEVEL"] = get_count($this->data["ROW_NUM"]);
        }
        Query::dbCheckIn($db);
        return true;
    }
    
    function &getUpdateNoModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
            knjz211fQuery::delete_no($this, $db);
            knjz211fQuery::insert_no($this, $db);
            knjz211fQuery::delete_yes($this, $db);
            knjz211fQuery::insert_yes($this, $db);
            $this->setMessage("MSG201");

            $this->field["MAX_ASSESSLEVEL"] = get_count($this->data["ROW_NUM"]);
        }
        Query::dbCheckIn($db);
        return true;
    }
    
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        if ($this->validateInput()) {
        	$div = $this->field["DIV"];
        	$this->field["DIV"] = 1;
            knjz211fQuery::delete($this, $db);
        	$this->field["DIV"] = 2;
            knjz211fQuery::delete($this, $db);
        	$this->field["DIV"] = $div;
            $this->setMessage("MSG201");

            $this->field["MAX_ASSESSLEVEL"] = null;
        }
        Query::dbCheckIn($db);
        return true;
    }
}
//var_dump($_REQUEST);
?>
