<?php

require_once('for_php7.php');

//定数定義
class knje464Model extends Model {
    var $field = array();
    var $cmd;
    var $facility_cd;       //特待区分コード(左画面選択時の値)

    //メンバ変数の初期化
    function knje464Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->facility_cd);
        }

        $this->cmd = VARS::request("cmd");

        if ($this->cmd != "reset") {
            if (VARS::get("SPRT_FACILITY_CD")) {
                $this->facility_cd = VARS::get("SPRT_FACILITY_CD");
            }

            $this->field = array("SPRT_FACILITY_CD"     => (VARS::post("SPRT_FACILITY_CD") == "" ? "" : sprintf("%03d", VARS::post("SPRT_FACILITY_CD"))),        //特待区分コード
                                 "SPRT_FACILITY_NAME"   => VARS::post("SPRT_FACILITY_NAME"),      //特待区分名称
                                 "UPDATED"              => VARS::post("UPDATED"));
        } else {
            $this->facility_cd = "";
            $this->field = array();
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["SPRT_FACILITY_CD"]) == 0) {
            $this->setWarning("MSG301","(コード)");
            return false;
        }
        
        if ($kind == "insert" || $kind == "update") {
            if(strlen($this->field["SPRT_FACILITY_NAME"]) == 0) {
                $this->setWarning("MSG301","(名称)");
                return false;
            }
        }
        //バイト数チェック
        if (strlen($this->field["SPRT_FACILITY_NAME"]) > 90) {
            $this->setWarning("MSG915","(名称は90バイトまでです。)");
            return false;
        }

        if (($kind=="update" || $kind=="delete") && $this->facility_cd != $this->field["SPRT_FACILITY_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knje464Query::getRow($this, $this->field["SPRT_FACILITY_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knje464Query::getRow($this, $this->facility_cd );
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row2)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knje464Query::getInsertQuery($this, $this->field);
            $this->facility_cd = sprintf("%02d",$this->field["SPRT_FACILITY_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knje464Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knje464Query::getDeleteQuery($this, $this->field);
            $this->facility_cd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knje464Query::CheckData($this, CTRL_YEAR);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knje464Query::CheckData($this, CTRL_YEAR - 1);
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knje464Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
