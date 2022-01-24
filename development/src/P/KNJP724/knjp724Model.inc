<?php

require_once('for_php7.php');

//定数定義
class knjp724Model extends Model {
    var $field = array();
    var $cmd;
    var $prefecturescd; //都道府県コード
    var $grade;         //学年
    var $year;

    //メンバ変数の初期化
    function knjp724Model() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->prefecturescd);
            unset($this->grade);
        }

        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }
        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        if (VARS::get("PREFECTURESCD")) {
            $this->prefecturescd = VARS::get("PREFECTURESCD");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }

        $this->field = array("YEAR"             => $this->year,
                             "PREFECTURESCD"    => VARS::post("PREFECTURESCD"),     //都道府県コード
                             "GRADE"            => VARS::post("GRADE"),             //学年
                             "REFER_YEAR_DIV1"  => VARS::post("REFER_YEAR_DIV1"),   //参照年度1
                             "REFER_YEAR_DIV2"  => VARS::post("REFER_YEAR_DIV2"),   //参照年度2
                             "STANDARD_SCHOOL_FEE"  => VARS::post("STANDARD_SCHOOL_FEE")    //標準授業料
                             );
    }

    //入力項目チェック
    function validateInput($kind) {
        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["YEAR"]) < 4) {
                $this->setWarning("MSG301","( 年度 )");
                return false;
            }
            if (strlen($this->field["PREFECTURESCD"]) == 0) {
                $this->setWarning("MSG301","( 都道府県コード )");
                return false;
            }
            if (strlen($this->field["GRADE"]) == 0) {
                $this->setWarning("MSG301","( 学年 )");
                return false;
            }
            for ($i = 1; $i <= 2; $i++) {
                if (strlen($this->field["REFER_YEAR_DIV".$i]) == 0) {
                    $this->setWarning("MSG301","( 参照年度{$i} )");
                    return false;
                }
            }
            //文字数チェック
            if (strlen($this->field["STANDARD_SCHOOL_FEE"]) > 8) {
                $this->setWarning("MSG915","( 標準授業料は8バイトまでです。)");
                return false;
            }
        }

        if (($kind=="update" || $kind=="delete") && ($this->prefecturescd != $this->field["PREFECTURESCD"] ||
                                                    $this->grade != $this->field["GRADE"])) {
            $this->setWarning("MSG308");
            return false;
        }

        $row = knjp724Query::getRow($this, 2);      //入力項目の１レコード取得
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjp724Query::getInsertQuery($this->field, $this);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];

            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjp724Query::getUpdateQuery($this->field, $this);
            $this->prefecturescd    = $this->field["PREFECTURESCD"];
            $this->grade            = $this->field["GRADE"];

            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp724Query::getDeleteQuery($this->field, $this);
            $this->prefecturescd    = "";
            $this->grade            = "";

            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と今年度のデータ存在チェック
        $check = array();
        $check = knjp724Query::CheckData($this->year, $this);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjp724Query::getCopyQuery($this->year, $this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>
