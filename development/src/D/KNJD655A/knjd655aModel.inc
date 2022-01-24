<?php

require_once('for_php7.php');

//定数定義
class knjd655aModel extends Model {
    var $field      = array();
    var $cmd;
    var $course_pk;
    var $subclasscd;
    var $coursename;
    var $coursecode;
    var $coursecd;
    var $majorcd;
    var $grade_hr_class;
    var $grade;
    var $hr_class;
    var $semester;
    var $testname;
    var $testcd;

    //メンバ変数の初期化
    function knjd655aModel() {}

    function init() {
        $this->cmd        = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        
        $this->subclasscd  = "000000";
        $this->coursecd    = "0";
        $this->majorcd     = "000";
        $this->coursecode  = "0000";
        $this->kind_div    = "3";

        if (VARS::get("PROGRAMID") || $this->cmd == "coursename") {
            $this->grade_hr_class = "";
            $this->grade = "";
            $this->hr_class = "";
            $this->testcd = "";
            $this->semester = "";
        }

        $this->field = array("FOOTNOTE"       =>  VARS::post("FOOTNOTE"),
                             "SEMESTER"       =>  VARS::post("SEMESTER"),
                             "TESTCD"         =>  VARS::post("TESTCD"),
                             "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"),
                             "UPDATED"        =>  VARS::post("UPDATED"),
                             "YEAR"           =>  CTRL_YEAR
                             );

        if (VARS::request("SEMESTER")) {
            $this->semester = VARS::request("SEMESTER");
        }
        if (VARS::request("TESTCD")) {
            $this->testcd   = VARS::request("TESTCD");
        }
        if (VARS::request("GRADE_HR_CLASS")) {
            $this->grade_hr_class = VARS::request("GRADE_HR_CLASS");
            $this->grade          = substr(VARS::request("GRADE_HR_CLASS"), 0, 2);
            $this->hr_class       = substr(VARS::request("GRADE_HR_CLASS"), 2);
        }
    }

    //入力項目チェック
    function validateInput() {
        //１レコード取得
        $row = knjd655aQuery::chkCODE($this);
        $Row = knjd655aQuery::getRow($this);
        if ($this->cmd=="update" || $this->cmd=="delete") {
           //更新日付チェック1
            if ($this->cmd == "update" && $Row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($this->cmd=="update" || $this->cmd=="add") {
           //重複チェック
           if ($this->cmd == "add" && is_array($row)) {
               $this->setWarning("MSG918");
               return false;
           }
           if ($this->cmd == "update" && !is_array($row)) {
               $this->setWarning("MSG303");
               return false;
           }
        }

        if ($this->validate_row_cnt($this->field["FOOTNOTE"], (45 * 2)) > 7) {
            $this->setWarning("MSG915","(本文は7行までです)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete() {
        $result = knjd655aQuery::getDeleteQuery($this);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert() {
        $result = knjd655aQuery::getInsertQuery($this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
?>
