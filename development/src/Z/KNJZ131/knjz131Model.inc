<?php

require_once('for_php7.php');

//定数定義
class knjz131Model extends Model {
    var $field = array();
    var $cmd;
    var $diCd;

    //メンバ変数の初期化
    function knjz131Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->diCd);
        }

        $this->cmd = VARS::request("cmd");
        if (VARS::post("YEAR")) {
            $this->year = VARS::post("YEAR");
        }

        if (strlen(VARS::get("SEND_DI_CD")) > 0) {
            $this->diCd = VARS::get("SEND_DI_CD");
        }

        $this->field = array("YEAR"              => VARS::post("YEAR"),
                             "DI_CD"             => VARS::post("DI_CD"),
                             "DI_NAME1"          => VARS::post("DI_NAME1"),
                             "DI_NAME2"          => VARS::post("DI_NAME2"),
                             "ATSUB_REPL_DI_CD"  => VARS::post("ATSUB_REPL_DI_CD"),
                             "DI_MARK"           => VARS::post("DI_MARK"),
                             "MULTIPLY"          => VARS::post("MULTIPLY"),
                             "RESTRICT_FLG"      => VARS::post("RESTRICT_FLG"),
                             "ONEDAY_DI_CD"      => VARS::post("ONEDAY_DI_CD"),
                             "ORDER"             => VARS::post("ORDER"),
                             "PETITION_ORDER"    => VARS::post("PETITION_ORDER"),
                             "REP_DI_CD"         => VARS::post("REP_DI_CD")
                             );

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["DI_CD"]) == 0) {
            $this->setWarning("MSG301","(出欠コード)");
            return false;
        }

        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["DI_NAME1"]) == 0) {
                $this->setWarning("MSG301","(出欠名称１)");
                return false;
            }
            //空文字チェック
            if (strlen($this->field["REP_DI_CD"]) == 0) {
                $this->setWarning("MSG301","(読替先処理コード)");
                return false;
            }
        }

        //バイト数チェック
        if (strlen($this->field["DI_NAME1"]) > 60) {
            $this->setWarning("MSG915","(出欠名称１は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["DI_NAME2"]) > 60) {
            $this->setWarning("MSG915","(出欠名称２は60バイトまでです。)");
            return false;
        }
        if (($kind=="update" || $kind=="delete") && $this->diCd != $this->field["DI_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjz131Query::getRightDataRow($this, $this->field["DI_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjz131Query::getRightDataRow($this, $this->diCd );
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
            knjz131Query::getInsertQuery($this, $this->field);
            $this->diCd = sprintf("%02d",$this->field["DI_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz131Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz131Query::getDeleteQuery($this, $this->field);
            $this->reductionDivCd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjz131Query::CheckData($this, $this->year);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjz131Query::CheckData($this, (int)$this->year - 1);
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjz131Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
