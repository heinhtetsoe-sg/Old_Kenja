<?php

require_once('for_php7.php');
class knjf090Model extends Model {

    var $field      = array();
    var $schregno;      //学籍番号
    var $cmd;
    var $name;          //生徒氏名
    var $date;          //日付
    var $time;          //時間
    var $treatment_div; //診療区分
    var $org_data;      //変更前のデータ
    //更新するフィールド
    var $item = array("DATE",
                      "DATE_H",
                      "DATE_M",
                      "PERIOD",
                      "TREATMENT_DIV",
                      "VISIT_REASON",
                      "OCCURTIMECD",
                      "OCCURTIME_H",
                      "OCCURTIME_M",
                      "TEMPERATURE",
                      "BREAKFAST",
                      "BEDTIME_H",
                      "BEDTIME_M",
                      "RISINGTIME_H",
                      "RISINGTIME_M",
                      "SLEEPING",
                      "NURSETREAT",
                      "REMARK"
                      );

    var $control_data = array();    //コントロールマスタデータを保持

    function knjf090Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }
    function init()
    {
        $this->cmd       = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("NAME"))  $this->name = VARS::get("NAME");
        if (VARS::get("DATE") && $this->cmd == "edit"){
            $this->date   = VARS::get("DATE");
        }else if (preg_match("/^[0-9]{4}\/[0-9]{2}\/[0-9]{2}$/i",VARS::post("DATE"))){
            $this->date = VARS::post("DATE");
        }else{
            unset($this->date);
        }
        if (VARS::get("TIME") != "" && $this->cmd == "edit"){
            $this->time   = VARS::get("TIME");
        }else if (is_numeric(VARS::post("DATE_H")) && is_numeric(VARS::post("DATE_M"))){
            $this->time = sprintf("%02d:%02d:00", VARS::post("DATE_H"),VARS::post("DATE_M"));
        }else{
            unset($this->time);
        }
        if (VARS::request("TREATMENT_DIV")){
            $this->treatment_div     = VARS::request("TREATMENT_DIV");
        }
        if(VARS::get("GRADE")){
            $this->grade = VARS::get("GRADE");
        }
        if(VARS::get("HR_CLASS")){
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if(VARS::get("ATTENDNO")){
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("init") == 1 || VARS::get("PROGRAMID")){
            unset($this->name);
            unset($this->schregno);
            unset($this->date);
            unset($this->time);
            unset($this->treatment_div);
        }
        if (VARS::get("SCHREGNO") != "") $this->schregno = VARS::get("SCHREGNO");
        foreach($this->item as $field){
            $this->field[$field] = VARS::post($field);
        }
    }
    //入力項目チェック
    function validateInput()
    {
        if($this->schregno ==""){
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "add"){
            $db = Query::dbCheckOut();
            //SQL文発行
            $query = knjf090Query::selectQuery($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (is_array($row)){
                $this->setWarning("MSG302");
                return false;
            }
            if (!isset($this->date) || !isset($this->time)){
                $this->setWarning("MSG301");
                return false;
            }
        }else if ($this->cmd == "update" || $this->cmd == "delete"){
            if ($this->org_data["DATE"] != str_replace("/","-",$this->date)){
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->org_data["TIME"] != $this->time){
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->org_data["TREATMENT_DIV_CD"] != $this->treatment_div){
                $this->setWarning("MSG308");
                return false;
            }
        }
/*        //必須項目チェック
        if (!strlen($this->field["DATE"]) || !strlen($this->field["DATE_H"] || !strlen($this->field["DATE_M"])
            || !strlen($this->field["PERIOD"]) || !strlen($this->field["TREATMENT_DIV"]))) {
            $this->setWarning("MSG301");
            return false;
        } */
        if(strlen($this->field["REMARK"]) > 120){
            $this->setWarning("MSG915","備考は120バイトまでです。");
            return false;
        }

        return true;
    }
    /**
     * 追加用のモデルを返します
    */
    function &getInsertModel()
    {
        if ($this->validateInput()){
            knjf090Query::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            knjf090Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
    /**
     * 削除用のモデルを返します
     */
    function &getDeleteModel()
    {
        if ($this->validateInput()){
            knjf090Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
    }
    function &delete()
    {
    }
    function &insert()
    {
    }
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
    }

}
?>
