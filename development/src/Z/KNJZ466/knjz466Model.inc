<?php

require_once('for_php7.php');

//定数定義
class knjz466Model extends Model {
    var $field = array();
    var $cmd;
    var $scholarship_cd;       //特待区分コード

    //メンバ変数の初期化
    function knjz466Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->scholarship_cd);
            unset($this->schoolKind);
        }

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        if (VARS::get("SCHOLARSHIP")) {
            $this->scholarship_cd = sprintf("%02d",VARS::get("SCHOLARSHIP"));
        }

        $this->field = array("SCHOLARSHIP"          => VARS::post("SCHOLARSHIP"),        //特待区分コード
                             "SCHOLARSHIP_NAME"     => VARS::post("SCHOLARSHIP_NAME"),      //特待区分名称
                             "SCHOLARSHIP_ABBV"     => VARS::post("SCHOLARSHIP_ABBV"),      //特待区分略称
                             "UPDATED"              => VARS::post("UPDATED"));

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["SCHOLARSHIP"]) == 0) {
            $this->setWarning("MSG301","(コード)");
            return false;
        }
        
        if ($kind == "insert" || $kind == "update") {
            if(strlen($this->field["SCHOLARSHIP_NAME"]) == 0) {
                $this->setWarning("MSG301","(名称)");
                return false;
            }
        }
        //バイト数チェック
        if (strlen($this->field["SCHOLARSHIP_NAME"]) > 90) {
            $this->setWarning("MSG915","(名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["SCHOLARSHIP_ABBV"]) > 15) {
            $this->setWarning("MSG915","(略称は15バイトまでです。)");
            return false;
        }

        if (($kind=="update" || $kind=="delete") && $this->scholarship_cd != $this->field["SCHOLARSHIP"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjz466Query::getRow($this, $this->field["SCHOLARSHIP"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjz466Query::getRow($this, $this->scholarship_cd );
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
            knjz466Query::getInsertQuery($this, $this->field);
            $this->scholarship_cd = sprintf("%02d",$this->field["SCHOLARSHIP"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz466Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz466Query::getDeleteQuery($this, $this->field);
            $this->scholarship_cd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjz466Query::CheckData($this, CTRL_YEAR);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjz466Query::CheckData($this, CTRL_YEAR - 1);
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjz466Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
