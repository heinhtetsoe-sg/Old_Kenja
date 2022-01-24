<?php

require_once('for_php7.php');

class knjz040_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $coursecd = "";        //primary

    function knjz040_2Model()
    {
    }

    function init()
    {              
        $this->field = array("COURSECD"     =>  VARS::post("COURSECD"),
                             "COURSENAME"   =>  VARS::post("COURSENAME"),
                             "COURSEABBV"   =>  VARS::post("COURSEABBV"),
                             "COURSEENG"    =>  VARS::post("COURSEENG"),
                             "S_PERIODCD"   =>  VARS::post("S_PERIODCD"),
                             "E_PERIODCD"   =>  VARS::post("E_PERIODCD"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->coursecd);
        }
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd = VARS::get("COURSECD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["COURSECD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(課程コード)");
            return false;
        }
        if ($this->coursecd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->coursecd != $this->field["COURSECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjz040_2Query::getRow($this->field["COURSECD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(課程コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["COURSENAME"]) == 0) {
            $this->setWarning("MSG301","(課程名称)");
            return false;
        } elseif (strlen($this->field["COURSENAME"]) > 30 ) {
            $this->setWarning("MSG915", "(課程名称) 30バイトまで");
            return false;
        } elseif (strlen($this->field["COURSEABBV"]) > 6 ) {
            $this->setWarning("MSG915", "(課程略称) 6バイトまで");
            return false;
        } elseif(strlen($this->field["COURSEENG"]) > 10) {
            $this->setWarning("MSG915", "(課程英字) 10バイトまで");
            return false;
        }
        //開始校時と終了校時の妥当性
        if($this->field["S_PERIODCD"] > $this->field["E_PERIODCD"]){
            $this->setWarning("MSG901", "開始校時が終了校時を超えています。");
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
            $this->coursecd = $this->field["COURSECD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->coursecd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->coursecd != $this->field["COURSECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjz040_2Query::getRow($this->coursecd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if (knjz040_2Query::IsExisting($this->coursecd)){
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }
        if (knjz040_2Query::checkMajormst($this->coursecd)){
            $this->setWarning("MSG919", "学科マスタに登録されています。");
            return;
        }
        knjz040_2Query::getDeleteQuery($this->field);
        $this->coursecd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjz040_2Query::getInsertQuery($this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz040_2Query::getUpdateQuery($this->field);
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
