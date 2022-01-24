<?php

require_once('for_php7.php');

//定数定義
class knjl502aModel extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $testdiv;
    var $hopeCoursecode;

    //メンバ変数の初期化
    function knjl502aModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->testdiv);
            unset($this->hopeCoursecode);
        }

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        $this->cmd = VARS::request("cmd");

        if (VARS::get("TESTDIV")) {
            $this->testdiv = VARS::get("TESTDIV");
        }
        if (VARS::get("HOPE_COURSECODE")) {
            $this->hopeCoursecode = VARS::get("HOPE_COURSECODE");
        }

        $this->field = array(
            "TESTDIV"             => VARS::post("TESTDIV"),             //入試区分
            "HOPE_COURSECODE"     => VARS::post("HOPE_COURSECODE"),     //志望区分
            "CLASS_SCORE"         => VARS::post("CLASS_SCORE"),         //各教科
            "SCORE5"              => VARS::post("SCORE5"),              //５科合計
            "SCORE9"              => VARS::post("SCORE9"),              //９科合計
            "HEALTH_PE_DISREGARD" => VARS::post("HEALTH_PE_DISREGARD"), //保体無視
        );

    }

    //入力項目チェック
    function validateInput($kind) {
        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["TESTDIV"]) == 0) {
                $this->setWarning("MSG301","( 入試区分 )");
                return false;
            }
            if (strlen($this->field["HOPE_COURSECODE"]) == 0) {
                $this->setWarning("MSG301","( 志望区分 )");
                return false;
            }

            //数値チェック
            if ($this->field["CLASS_SCORE"] != "" && !is_numeric($this->field["CLASS_SCORE"])) {
                $this->setWarning("MSG907","( 各教科 )");
                return false;
            }
            if ($this->field["SCORE5"] != "" && !is_numeric($this->field["SCORE5"])) {
                $this->setWarning("MSG907","( ５科合計 )");
                return false;
            }
            if ($this->field["SCORE9"] != "" && !is_numeric($this->field["SCORE9"])) {
                $this->setWarning("MSG907","( ９科合計 )");
                return false;
            }

            //文字数チェック
            if (strlen($this->field["CLASS_SCORE"]) > 2) {
                $this->setWarning("MSG915","( 各教科は2バイトまでです。)");
                return false;
            }
            if (strlen($this->field["SCORE5"]) > 2) {
                $this->setWarning("MSG915","( ５科合計は2バイトまでです。)");
                return false;
            }
            if (strlen($this->field["SCORE9"]) > 2) {
                $this->setWarning("MSG915","( ９科合計は2バイトまでです。)");
                return false;
            }
        }

        if (($kind=="update" || $kind=="delete") &&
            ($this->testdiv != $this->field["TESTDIV"] || $this->hopeCoursecode != $this->field["HOPE_COURSECODE"])) {
            $this->setWarning("MSG308");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjl502aQuery::getRow($this);
        //存在チェック
        if (($kind == "insert") && is_array($row)) {
            $this->setWarning("MSG302");
            return false;
        }
        if (($kind == "update" || $kind == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl502aQuery::getInsertQuery($this);
            $this->testdiv        = $this->field["TESTDIV"];
            $this->hopeCoursecode = $this->field["HOPE_COURSECODE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl502aQuery::getUpdateQuery($this, $this->field, $this->reduction_seq);
            $this->testdiv        = $this->field["TESTDIV"];
            $this->hopeCoursecode = $this->field["HOPE_COURSECODE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl502aQuery::getDeleteQuery($this);
            $this->testdiv        = "";
            $this->hopeCoursecode = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と前年度のデータ存在チェック
        $check = knjl502aQuery::CheckData($this);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjl502aQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>
