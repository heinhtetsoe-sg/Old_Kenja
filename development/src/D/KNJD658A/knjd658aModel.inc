<?php

require_once('for_php7.php');

//定数定義
class knjd658aModel extends Model {
    var $field      = array();
    var $cmd;
    var $course_pk;
    var $subclasscd;
    var $coursecode;
    var $coursecd;
    var $majorcd;
    var $hr_class;
    var $semester;
    var $testname;
    var $grade;
    var $moji;
    var $gyou;

    //メンバ変数の初期化
    function knjd658aModel() {}

    function init() {
        $this->getPropertiesAll(); //プロパティファイル
    
        $this->cmd         = VARS::request("cmd");
        $this->moji = 50;
        $this->gyou = 3;

        $this->semester    = "0";
        $this->testkindcd  = "00";
        $this->testitemcd  = "00";
        $this->testcd      = $this->testkindcd . $this->testitemcd;
        $this->hr_class    = "000";
        $this->coursecd    = "0";
        $this->majorcd     = "000";
        $this->coursecode  = "0000";
        $this->subclasscd  = "000000";
        $this->kind_div    = "4";

        if (VARS::get("PROGRAMID")) {
            $this->grade = "";
        }

        $this->field = array("FOOTNOTE" => VARS::post("FOOTNOTE"),
                             "GRADE"    => VARS::post("GRADE"),
                             "UPDATED"  => VARS::post("UPDATED"),
                             "YEAR"     => CTRL_YEAR
                             );

        if (VARS::request("GRADE")) {
            $this->grade    = VARS::request("GRADE");
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->validate_row_cnt($this->field["FOOTNOTE"], ($this->moji * 2)) > $this->gyou) {
            $this->setWarning("MSG915","(本文は{$this->gyou}行までです)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjd658aQuery::getInsertQuery($this);
            if (!isset($model->warning)) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $query = knjd658aQuery::getCopyCount($this);
        $copyCount = $db->getOne($query);
        if ($copyCount < 1) {
            $this->setMessage("更新対象がありませんでした。");
            return;
        }

        //一旦削除
        $query = knjd658aQuery::getDeleteQuery($this);
        $db->query($query);

        $query = knjd658aQuery::copyInsert($this);
        $db->query($query);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
    }
}
?>
