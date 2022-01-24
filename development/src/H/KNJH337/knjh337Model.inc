<?php

require_once('for_php7.php');

class knjh337Model extends Model {
    var $field      = array();
    var $cmd;
    var $mock_subclass_cd = "";        //primary
    var $thisIsGet = false;

    function knjh337Model()
    {
    }

    function init()
    {   
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->field = array("MOCK_SUBCLASS_CD" =>  VARS::post("MOCK_SUBCLASS_CD"),
                             "SUBCLASS_NAME"    =>  VARS::post("SUBCLASS_NAME"),
                             "SUBCLASS_ABBV"    =>  VARS::post("SUBCLASS_ABBV"),
                             "CLASSCD"          =>  VARS::post("CLASSCD"),
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),
                             "PREF_SUBCLASSCD"  =>  VARS::post("PREF_SUBCLASSCD"),
                             "SUBCLASS_DIV"     =>  VARS::post("SUBCLASS_DIV")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->mock_subclass_cd);
        }
        $temp = VARS::get("MOCK_SUBCLASS_CD");
        if (isset($temp)){
            $this->mock_subclass_cd = VARS::get("MOCK_SUBCLASS_CD");
            $this->thisIsGet = true;
        }else if($this->cmd == "reset"){
            $this->thisIsGet = true;
        } else {
            $this->thisIsGet = false;
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["MOCK_SUBCLASS_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(模試科目コード)");
            return false;
        }
        if (strlen($this->field["SUBCLASS_NAME"]) == 0) {
            $this->setWarning("MSG301","(模試科目名称)");
            return false;
        }
        if (strlen($this->field["SUBCLASS_ABBV"]) == 0) {
            $this->setWarning("MSG301","(模試科目略称)");
            return false;
        }
        if (strlen($this->field["CLASSCD"]) == 0 && $this->Properties["usePerfSubclasscd_Touroku"] == '1') {
            $this->setWarning("MSG301","(教科コード)");
            return false;
        }

        if ($this->mock_subclass_cd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->mock_subclass_cd != $this->field["MOCK_SUBCLASS_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $db = Query::dbCheckOut(); //DB接続
        $row = knjh337Query::getRow($this->field["MOCK_SUBCLASS_CD"], $db, $this);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(模試科目コード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["SUBCLASS_NAME"] = array("LEN" => 60, "MESAGE" => "(模試科目名称)");
        $checkStrLen["SUBCLASS_ABBV"] = array("LEN" => 15,  "MESAGE" => "(模試科目略称)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->mock_subclass_cd = $this->field["MOCK_SUBCLASS_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->mock_subclass_cd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->mock_subclass_cd != $this->field["MOCK_SUBCLASS_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $db = Query::dbCheckOut(); //DB接続
        $row = knjh337Query::getRow($this->mock_subclass_cd, $db, $this);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        knjh337Query::getDeleteQuery($this->field);
        $this->mock_subclass_cd = "";
        $this->setMessage("MSG201");
        
        $this->field = array();
    }

    //インサート
    function &insert()
    {
        $result = knjh337Query::getInsertQuery($this->field, $this);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh337Query::getUpdateQuery($this->field, $this);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
