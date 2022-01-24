<?php

require_once('for_php7.php');

//定数定義
class knjd106dModel extends Model {

    var $field      = array();
    var $cmd;
    var $course_pk;         //primary
    var $subclasscd;
    var $coursename;
    var $coursecode;
    var $coursecd;
    var $majorcd;
    var $grade;
    var $semester;
    var $testname;
    var $proficiencydiv;
    var $proficiencycd;

    //メンバ変数の初期化
    function knjd106dModel()
    {
    }

    function init()
    {

        $this->cmd        = VARS::request("cmd");

        $this->subclasscd = "999999";

        if (VARS::get("PROGRAMID") || $this->cmd == "coursename"){
            $this->coursecode = "";
            $this->coursecd = "";
            $this->majorcd = "";
            $this->grade = "";
            $this->proficiencydiv = "";
            $this->proficiencycd = "";
            $this->semester = "";
        }

        $this->field = array("COURSE"       =>  VARS::post("COURSE"),
                             "FOOTNOTE"     =>  VARS::post("FOOTNOTE"),
                             "SEMESTER"     =>  VARS::post("SEMESTER"),
                             "PROFICIENCYDIV" =>  VARS::post("PROFICIENCYDIV"),
                             "PROFICIENCYCD" =>  VARS::post("PROFICIENCYCD"),
                             "GRADE"        =>  VARS::post("GRADE") ? VARS::post("GRADE") : $this->grade,
                             "UPDATED"      =>  VARS::post("UPDATED"),
                             "YEAR"         =>  CTRL_YEAR
                             );
        if (VARS::request("SEMESTER")){
            $this->semester = VARS::request("SEMESTER");
        }
        if (VARS::request("PROFICIENCYDIV")){
            $this->proficiencydiv = VARS::request("PROFICIENCYDIV");
        }
        if (VARS::request("PROFICIENCYCD")){
            $this->proficiencycd = VARS::request("PROFICIENCYCD");
        }
        if (VARS::request("GRADE")){
            $this->grade = VARS::request("GRADE");
        }
        if (VARS::get("COURSECODE")){
            $this->coursecode = VARS::get("COURSECODE");
        }
        if (VARS::get("COURSECD")){
            $this->coursecd = VARS::get("COURSECD");
        }
        if (VARS::get("MAJORCD")){
            $this->majorcd = VARS::get("MAJORCD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //１レコード取得
        $row = knjd106dQuery::chkCode($this);
        if ($this->cmd == "update" || $this->cmd == "delete") {
            //リストより選択していない
            if ($this->coursecode == "" &&
                $this->coursecd == "" &&
                $this->majorcd == ""
            ) {
                $this->setWarning("MSG308");
                return false;
            }
            //キー項目を変更した場合
            if($this->coursecd."-".$this->majorcd."-".$this->coursecode != $this->field["COURSE"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //重複チェック
        if ($this->cmd == "update" && !is_array($row)){
            $this->setWarning("対象データがありません。");
            return false;
        }

        if (strlen($this->field["FOOTNOTE"]) > 957) {
            $this->setWarning("MSG915","(全体評は957バイトまでです)");
            return false;
        }

        return true;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $result = $db->query(knjd106dQuery::getOldYear($this));   //前年度のデータ取得
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $db->query(knjd106dQuery::copyInsert($row));             //前年度のデータを今年度のデータとしてINSERT
        }
        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
        }
        return;
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert()
    {
        knjd106dQuery::getInsertQuery($this);
        if (!isset($model->warning)){
            list($this->coursecd, $this->majorcd, $this->coursecode) = preg_split("/-/", $this->field["COURSE"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete()
    {
        $result = knjd106dQuery::getDeleteQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
            $this->coursecode = "";
            $this->coursecd   = "";
            $this->majorcd    = "";
        }
    }

}
?>
