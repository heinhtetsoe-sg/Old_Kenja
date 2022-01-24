<?php

require_once('for_php7.php');

class knji110Model extends Model {
    var $fields      = array();
    var $cmd;
    var $cmd2;
    var $hr_class;        //primary
    var $grade;
    var $term;
    var $term2;
    var $control;

    function knji110Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->term);
            unset($this->term2);
        }
        if (VARS::request("term") != "") {
            $this->term  = VARS::request("term");
        }
        if (VARS::request("term2") != "") {
            $this->term2 = VARS::request("term2");
        }

        $this->fields = array("GRADE"           =>  VARS::post("GRADE"),
                              "HR_CLASS"        =>  VARS::post("HR_CLASS"),
                              "HR_NAME"         =>  VARS::post("HR_NAME"),
                              "HR_NAMEABBV"     =>  VARS::post("HR_NAMEABBV"),
                              "HR_CLASS_NAME1"  =>  VARS::post("HR_CLASS_NAME1"),
                              "HR_CLASS_NAME2"  =>  VARS::post("HR_CLASS_NAME2"),
                              "GRADE_NAME"      =>  VARS::post("GRADE_NAME"),
                              "TR_CD1"          =>  VARS::post("TR_CD1"),
                              "TR_CD2"          =>  VARS::post("TR_CD2"),
                              "TR_CD3"          =>  VARS::post("TR_CD3"),
                              "SUBTR_CD1"       =>  VARS::post("SUBTR_CD1"),
                              "SUBTR_CD2"       =>  VARS::post("SUBTR_CD2"),
                              "SUBTR_CD3"       =>  VARS::post("SUBTR_CD3"),
                              "HR_FACCD"        =>  VARS::post("HR_FACCD"),
                              "CLASSWEEKS"      =>  VARS::post("CLASSWEEKS"),
                              "CLASSDAYS"       =>  VARS::post("CLASSDAYS"),
                              "CTRL_YEAR"       =>  VARS::post("CTRL_YEAR"),
                              "CTRL_SEMESTER"   =>  VARS::post("CTRL_SEMESTER"),
                              "CTRL_DATE"       =>  VARS::post("CTRL_DATE"),
                              "UPDATED"         =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        $this->cmd2         = VARS::request("cmd2");
        $temp = VARS::get("HR_CLASS");
        if (isset($temp)) {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        $temp = VARS::get("GRADE");
        if (isset($temp)) {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("init") == "1") {
            unset($this->grade);
            unset($this->hr_class);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->cmd == "add" && (trim($this->fields["GRADE"]) == "" || trim($this->fields["HR_CLASS"]) == "")) {
            $this->setWarning("MSG301", "(学年および組)");
            return false;
        }
        if ($this->cmd == "add" && (trim($this->fields["HR_NAME"]) == "" || trim($this->fields["HR_NAMEABBV"]) == "")) {
            $this->setWarning("MSG301", "(組名称および組略称)");
            return false;
        }
        //キーの更新チェック
        if ($this->cmd == "update" && ($this->grade != $this->fields["GRADE"] || $this->hr_class != $this->fields["HR_CLASS"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && (trim($this->fields["HR_NAME"]) == "" || trim($this->fields["HR_NAMEABBV"]) == "")) {
            $this->setWarning("MSG301", "(組名称および組略称)");
            return false;
        }
        if ((!strlen($this->grade) || !strlen($this->hr_class)) && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }
        //学年ゼロ埋め
        if (strlen($this->fields["GRADE"]) == 1 ) {
            $this->fields["GRADE"] = sprintf("%02d", $this->fields["GRADE"]);
        }
        //組ゼロ埋め
        if (strlen($this->fields["HR_CLASS"]) == 1 ) {
            //2004/08/19 arakaki $this->fields["HR_CLASS"] = sprintf("%02d", $this->fields["HR_CLASS"]);
            $this->fields["HR_CLASS"] = sprintf("%03s", $this->fields["HR_CLASS"]);
        }
        //１レコード取得
        $row = knji110Query::getRow($this, $this->term, $this->fields["GRADE"], $this->fields["HR_CLASS"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(学年および組)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->fields["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->fields["GRADE"]) > 2 ) {
            $this->setWarning("MSG915", "(学年) 2バイトまでです。");
            return false;
        } elseif (strlen($this->fields["HR_CLASS"]) > 3 ) {
            $this->setWarning("MSG915", "(組) 3バイトまでです。");
            return false;
        } elseif (strlen($this->fields["HR_NAME"]) > 15 ) {
            $this->setWarning("MSG915", "(組名称) 15バイトまでです。");
            return false;
        } elseif (strlen($this->fields["HR_NAMEABBV"]) > 5 ) {
            $this->setWarning("MSG915", "(組略称) 5バイトまでです。");
            return false;
        } elseif (strlen($this->fields["CLASSWEEKS"]) > 2 ) {
            $this->setWarning("MSG915", "(授業週数) 2桁までです。");
            return false;
        } elseif (strlen($this->fields["CLASSDAYS"]) > 3 ) {
            $this->setWarning("MSG915", "(授業日数) 3桁までです。");
            return false;
        }
        //「コピーボタン」押し時に,クラス編成データに割り振られている生徒がいるかをチェック
        if($this->cmd =="copy"){
            $count = knji110Query::Count($this, $this->term);
            if($count > "0"){
                $this->warning="生徒が既に割り振られています。生徒HR割振り処理で削除してから再度実行してください。";
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
            $this->grade = $this->fields["GRADE"];
            $this->hr_class = $this->fields["HR_CLASS"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if (!strlen($this->grade) || !strlen($this->hr_class)) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->hr_class != $this->fields["HR_CLASS"] || $this->grade != $this->fields["GRADE"]) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $row = knji110Query::getRow($this, $this->term, $this->fields["GRADE"], $this->fields["HR_CLASS"]);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        $ret_val = knji110Query::getDeleteQuery($this);
        if ($ret_val == "class_formation") {
            $this->warning = "生徒が既に割り振られています。生徒HR割振り処理で削除してから再度実行してください。";
            return false;
        } elseif ($ret_val == "schreg_regd"){
            $this->warning = "クラスが存在するので削除できません。";
            return false;
        }
        unset($this->grade);
        unset($this->hr_class);
        $this->setMessage("MSG201");
    }

    //INSERT
    function &insert()
    {
        $result = knji110Query::getInsertQuery($this);
        return $result;
    }

    //UPDATE
    function &update()
    {
        $result = knji110Query::getUpdateQuery($this);
        return $result;
    }

    //コピーボタン押し時の処理
    function &getInsertCopyModel()
    {
        //入力チェック
        if($this->validateInput())
        {
            knji110Query::getInsertCopyQuery($this->term2,$this->term);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
