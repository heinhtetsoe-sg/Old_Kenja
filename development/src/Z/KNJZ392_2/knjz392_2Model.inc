<?php

require_once('for_php7.php');

class knjz392_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $fare_cd = "";        //primary

    function knjz392_2Model()
    {
    }

    function init()
    {              
        $this->field = array("FARE_CD"      =>  VARS::post("FARE_CD"),
                             "FARE"         =>  VARS::post("FARE"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->fare_cd);
        }
        $temp = VARS::get("FARE_CD");
        if (isset($temp)){
            $this->fare_cd = VARS::get("FARE_CD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["FARE_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(駅コード)");
            return false;
        }
        if ($this->fare_cd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->fare_cd != $this->field["FARE_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //預かりコードのゼロ埋め
        if (strlen($this->field["FARE_CD"]) < 2) {
            $this->field["FARE_CD"] = sprintf("%02d",$this->field["FARE_CD"]);
        }
        //１レコード取得
        $row = knjz392_2Query::getRow($this->field["FARE_CD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(預かりコード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
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
            $this->fare_cd = $this->field["FARE_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->fare_cd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->fare_cd != $this->field["FARE_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjz392_2Query::getRow($this->fare_cd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if (knjz392_2Query::IsExisting($this->fare_cd)){
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }
          knjz392_2Query::getDeleteQuery($this->field);
        $this->fare_cd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjz392_2Query::getInsertQuery($this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz392_2Query::getUpdateQuery($this->field);
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
