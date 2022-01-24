<?php

require_once('for_php7.php');

//定数定義
class knjp830Model extends Model {
    var $field = array();
    var $cmd;
    var $year;

    //メンバ変数の初期化
    function knjp830Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->grade);
            unset($this->gradeHrClass);
            unset($this->hrClassName);
        }

        $this->cmd = VARS::request("cmd");

        $this->year  = VARS::request("YEAR");
        $this->grade = VARS::request("GRADE");

        if (VARS::get("GRADE_HR_CLASS") != '') {
            $this->gradeHrClass = VARS::get("GRADE_HR_CLASS");
            $this->hrClassName  = VARS::get("HR_NAME");
        }

        $this->field = array("SGL_SCHOOLKIND"   => VARS::post("SGL_SCHOOLKIND"),    //設置
                             "SGL_MAJORCD"      => VARS::post("SGL_MAJORCD"),       //学部
                             "SGL_COURSECODE"   => VARS::post("SGL_COURSECODE")     //学科
                             );

        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput($kind) {

        //リストよりデータを選択してイルカ
        if ($this->gradeHrClass == '') {
            $this->setWarning("MSG309");
            return false;
        }

        //空文字チェック
        if ($kind == "insert" || $kind == "update") {
            if (strlen($this->field["SGL_SCHOOLKIND"]) == 0) {
                $this->setWarning("MSG301","(設置)");
                return false;
            }
            if (strlen($this->field["SGL_MAJORCD"]) == 0) {
                $this->setWarning("MSG301","(学部)");
                return false;
            }
            if (strlen($this->field["SGL_COURSECODE"]) == 0) {
                $this->setWarning("MSG301","(学科)");
                return false;
            }
        }

        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjp830Query::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjp830Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp830Query::getDeleteQuery($this, $this->field);
            $this->gradeHrClass = "";
            $this->hrClassName  = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp830Query::CheckData($this, $this->year);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp830Query::CheckData($this, $this->year - 1);
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp830Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
