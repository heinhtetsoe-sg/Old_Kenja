<?php

require_once('for_php7.php');

//定数定義
class knjd136Model extends Model {
    var $field      = array();
    var $cmd;
    var $course_pk;         //primary
    var $classcd;
    var $school_kind;
    var $curriculum_cd;
    var $subclasscd;
    var $coursename;
    var $coursecode;
    var $coursecd;
    var $majorcd;
    var $grade;
    var $semester;
    var $testname;
    var $testcd;

    //メンバ変数の初期化
    function knjd136Model() {}

    function init() {
        $this->cmd        = VARS::request("cmd");

        $this->classcd          = "00";
        $this->curriculum_cd    = "00";
        $this->subclasscd       = "000000";
        $this->coursecd         = "0";
        $this->majorcd          = "000";
        $this->coursecode       = "0000";
        $this->hr_class         = "000";
        $this->kind_div         = "2";
        $this->testcd           = '9900';

        if (VARS::get("PROGRAMID") || $this->cmd == "coursename") {
            $this->semester = "";
            $this->grade = "";
        }

        $this->field = array("FOOTNOTE"     =>  VARS::post("FOOTNOTE"),
                             "SEMESTER"     =>  VARS::post("SEMESTER"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "UPDATED"      =>  VARS::post("UPDATED"),
                             "YEAR"         =>  CTRL_YEAR
                             );

        if (VARS::request("SEMESTER")) {
            $this->semester = VARS::request("SEMESTER");
        }
        if (VARS::request("GRADE")) {
            $this->grade    = VARS::request("GRADE");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //１レコード取得
        $row = knjd136Query::chkCODE($this);
        $Row = knjd136Query::getRow($this);
        if ($this->cmd=="update" || $this->cmd=="delete") {
           //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
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

        if ($this->validate_row_cnt($this->field["FOOTNOTE"], ($this->getPro["FOOTNOTE"]["moji"] * 2)) > $this->getPro["FOOTNOTE"]["gyou"]) {
            $this->setWarning("MSG915","(本文は{$this->getPro["FOOTNOTE"]["gyou"]}行までです)");
            return false;
        }

        return true;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $query = knjd136Query::getOldYear($this);   //前年度のデータ取得
        $result    = $db->query($query);
        $exist_flg = false;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $db->query(knjd136Query::copyInsert($row, $this));          //前年度のデータを今年度のデータとしてINSERT
            $exist_flg = true;
        }
        if ($exist_flg) {
            $this->setWarning("MSG201");
        } else {
            $this->setWarning("更新対象が存在しません。");
        }
        Query::dbCheckIn($db);
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
        $result = knjd136Query::getDeleteQuery($this);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert() {
        $result = knjd136Query::getInsertQuery($this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
?>
