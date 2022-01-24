<?php

require_once('for_php7.php');

class knjz370Model extends Model {
    var $cmd;
    var $field;
    var $select_grade;
    var $asses;
    var $grade;
    var $year;
    var $flg;

    //メンバ変数の初期化
    function knjz370Model()
    {
    }
    
    function init()
    {
        $this->year = CTRL_YEAR;
        
        $this->field = array("YEAR"             => $this->year,
                             "GRADE"            => $this->select_grade,
                             "COURSE_SEQ"       => VARS::post("COURSE_SEQ"),
                             "GROUP_NAME"       => VARS::post("GROUP_NAME"),
                             "SELECTVALUE"      => VARS::post("selectvalue")
                             );

        $temp = VARS::request("GRADE");
        if(isset($temp)){
            $this->select_grade = VARS::request("GRADE");
        }
        $temp = VARS::get("COURSE_SEQ");
        if (isset($temp)) {
            $this->course_seq = VARS::get("COURSE_SEQ");
        }
        $temp = VARS::get("GROUP_NAME");
        if (isset($temp)) {
            $this->group_name = VARS::get("GROUP_NAME");
        }

        $temp = VARS::request("cmd");
        if (isset($temp)) {
            $this->cmd          = VARS::request("cmd");
        } else {
            unset($this->cmd);
            unset($this->select_grade);
            unset($this->course_seq);
            unset($this->group_name);
            unset($this->asses);
        }
    }
    
    function validateInput($cmd_kind)
    {
        if($cmd_kind == "add"){
            if(trim($this->field["COURSE_SEQ"]) == ""){
                $this->setWarning("MSG301", "(グループコード)");
                return false;
            }
        }
        if($cmd_kind == "update" || $cmd_kind == "delete"){
            if(trim($this->field["COURSE_SEQ"]) == "") {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->course_seq != $this->field["COURSE_SEQ"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        if ($cmd_kind == "add" || $cmd_kind == "update") {
            if(trim($this->field["SELECTVALUE"]) == ""){
                $this->setWarning("MSG301", "(受講クラス)");
                return false;
            }
            if(strlen($this->field["GROUP_NAME"]) > 45){
                $this->setWarning("MSG915", "(グループ名) 45バイトまで");
                return false;
            }
        }
        //グループコードの 0 埋め
        if(strlen($this->field["COURSE_SEQ"]) < 4){
            $this->field["COURSE_SEQ"] = sprintf("%04d", $this->field["COURSE_SEQ"]);
        }
        //存在チェック
        if($cmd_kind == "add"){
            if((knjz370Query::IsExistingCd($this->field)) > 0){
                $this->setWarning("MSG918","(グループコード)");
                return false;
            }
            elseif((knjz370Query::IsExistingCl($this->field)) > 0){
                $this->setWarning("MSG918","(グループクラス)");
                return false;
            }
        }
        return true;
    }

    function &getInsertModel()
    {
        if ($this->validateInput("add")) {
            $this->insert();
            $this->course_seq = $this->field["COURSE_SEQ"];
            $this->setMessage("MSG201");
        }
        $this->selectvalue = $this->field["selectvalue"];
    }
    function &getUpdateModel()
    {
        if ($this->validateInput("update")) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    function &getDeleteModel()
    {
        if ($this->validateInput("delete")) {
            $this->delete();
            $this->setMessage("MSG201");
        }
    }
    function &getCopyYearModel()
    {
        knjz370Query::getCopyYearQuery($this->year);
    }
    
    function &insert()
    {
        knjz370Query::getInsertQuery($this->field);
    }
    function &update()
    {
        knjz370Query::getDeleteQuery($this->field);
        knjz370Query::getInsertQuery($this->field);
    }
    function &delete()
    {
        knjz370Query::getDeleteQuery($this->field);
    }
}
?>
