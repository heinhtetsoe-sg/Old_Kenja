<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp707Model extends Model {
    var $field = array();
    var $cmd;
    var $collectPatternCd;         //パターンコード

    /**
     * メンバ変数の初期化
     */
    function knjp707Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->schoolKind);
            unset($this->collectPatternCd);
        }

        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        $this->monthArray = array("4", "5", "6", "7", "8", "9", "10", "11", "12", "1", "2", "3");

        $this->field = array("COLLECT_PATTERN_CD"       =>  VARS::post("COLLECT_PATTERN_CD"),       //入金パターンコード
                             "COLLECT_PATTERN_NAME"     =>  VARS::post("COLLECT_PATTERN_NAME"),     //パターン名
                             "DIRECT_DEBIT_DATE"        =>  VARS::post("DIRECT_DEBIT_DATE"),        //引き落とし日
                             "PAY_DIV"                  =>  VARS::post("PAY_DIV"));                 //入金方法

        foreach ($this->monthArray as $key => $val) {
            $setName = "COLLECT_MONTH_{$val}";
            $this->field[$setName] = VARS::post($setName);
        }

        if (VARS::get("COLLECT_PATTERN_CD")) {
            $this->collectPatternCd = VARS::get("COLLECT_PATTERN_CD");
        }

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjp707Query::getInsertQuery($this);
            $this->collectPatternCd = sprintf("%02d", $this->field["COLLECT_PATTERN_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if($this->validateInput("update")) {
            knjp707Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp707Query::getDeleteQuery($this);
            $this->collectPatternCd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //入力項目チェック
    function validateInput($cmd) {
        if ($cmd == "insert" || $cmd == "update") {
            //空文字チェック
            if (strlen($this->field["COLLECT_PATTERN_CD"]) == 0) {
                $this->setWarning("MSG301","( 入金パターンコード )");
                return false;
            }
            if (strlen($this->field["DIRECT_DEBIT_DATE"]) == 0) {
                $this->setWarning("MSG301","( 引き落とし日 )");
                return false;
            }
            $monthChkFlg = false;
            foreach ($this->monthArray as $key => $val) {
                if (strlen($this->field["COLLECT_MONTH_{$val}"]) != 0) {
                    $monthChkFlg = true;
                }
            }
            if (!$monthChkFlg) {
                $this->setWarning("MSG301","( 入金月設定 )");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["COLLECT_PATTERN_CD"]) > 2) {
                $this->setWarning("MSG915","( パターンコードは2バイトまでです。)");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["COLLECT_PATTERN_NAME"]) > 90) {
                $this->setWarning("MSG915","( パターン名は90バイトまでです。)");
                return false;
            }

            //値チェック
            if (sprintf("%02d",$this->field["COLLECT_PATTERN_CD"]) == '00') {
                $this->setWarning("MSG901","00は対象外です。\\n( 入金パターンコード )");
                return false;
            }
        }
        //コードのゼロ埋め
        if ($this->field["COLLECT_PATTERN_CD"] !="" && strlen($this->field["COLLECT_PATTERN_CD"]) < 2) {
            $this->field["COLLECT_PATTERN_CD"] = sprintf("%02d", $this->field["COLLECT_PATTERN_CD"]);
        }

        if (($cmd=="update" || $cmd=="delete") && ($this->collectPatternCd != $this->field["COLLECT_PATTERN_CD"])) {
            $this->setWarning("MSG308");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjp707Query::getRow($this, $this->field["COLLECT_PATTERN_CD"]);
        //重複チェック
        if ($cmd == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        return true;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp707Query::CheckData($this, CTRL_YEAR);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp707Query::CheckData($this, CTRL_YEAR - 1);
        if(!isset($pre_year)){
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp707Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
