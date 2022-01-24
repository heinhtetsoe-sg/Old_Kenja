<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp374_1Model extends Model {
    var $field = array();
    var $cmd;
    var $collectPatternCd;         //パターンコード

    /**
     * メンバ変数の初期化
     */
    function knjp374_1Model() {
    }

    function init() {
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId         = VARS::get("SEND_PRGID");
            $this->sendAuth         = VARS::get("SEND_AUTH");
            $this->sendGrade        = VARS::get("SEND_GRADE");
            $this->sendHrClass      = VARS::get("SEND_HR_CLASS");
            $this->sendSchoolKind   = VARS::get("SEND_SCHOOL_KIND");
            $this->schoolKind       = $this->sendSchoolKind;
        }
        $this->auth = $this->sendAuth;

        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;
        if (strlen(VARS::get("TRANSFER_DIV"))) {
            $this->transferDiv = VARS::get("TRANSFER_DIV");
        }

        $this->field = array("TRANSFER_DIV"     =>  VARS::post("TRANSFER_DIV"),
                             "TRANSFER_NAME"    =>  VARS::post("TRANSFER_NAME")
                            );

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == 'changeKind') {
            $this->transferDiv = '';
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjp374_1Query::getInsertQuery($this);
            $this->transferDiv = sprintf("%02d", $this->field["TRANSFER_DIV"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if($this->validateInput("update")) {
            knjp374_1Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp374_1Query::getDeleteQuery($this);
            $this->collectPatternCd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //入力項目チェック
    function validateInput($cmd) {
        if ($cmd == "insert" || $cmd == "update") {
            //空文字チェック
            if (strlen($this->field["TRANSFER_DIV"]) == 0) {
                $this->setWarning("MSG301","( 振込区分CD )");
                return false;
            }
            if (strlen($this->field["TRANSFER_NAME"]) == 0) {
                $this->setWarning("MSG301","( 振込区分名称 )");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["TRANSFER_DIV"]) > 2) {
                $this->setWarning("MSG915","( 振込区分CDは2バイトまでです。)");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["TRANSFER_NAME"]) > 90) {
                $this->setWarning("MSG915","( 振込区分名称は90バイトまでです。)");
                return false;
            }
        }
        //コードのゼロ埋め
        $this->field["TRANSFER_DIV"] = sprintf("%02d", $this->field["TRANSFER_DIV"]);

        if (($cmd=="update" || $cmd=="delete") && ($this->transferDiv != $this->field["TRANSFER_DIV"])) {
            $this->setWarning("MSG308");
            return false;
        }

        //入力項目の１レコード取得
        $db = Query::dbCheckOut();

        $query = knjp374_1Query::getOneRecord($this, $this->field["TRANSFER_DIV"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //重複チェック
        if ($cmd == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        Query::dbCheckIn($db);

        return true;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp374_1Query::CheckData($this, CTRL_YEAR);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp374_1Query::CheckData($this, CTRL_YEAR - 1);
        if(!isset($pre_year)){
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp374_1Query::getCopyQuery($this);
        $this->transferDiv = "";
        $this->setMessage("MSG201");
        return;
    }
}
?>
