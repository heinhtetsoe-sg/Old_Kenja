<?php

require_once('for_php7.php');

//定数定義
class knjp987Model extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $taxCd;
    var $dateFrom;

    //メンバ変数の初期化
    function knjp987Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->taxCd);
            unset($this->dateFrom);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        if (VARS::get("TAX_CD")) {
            $this->taxCd    = VARS::get("TAX_CD");
            $this->dateFrom = VARS::get("DATE_FROM");
        }

        $this->field = array("YEAR"         => $this->year,
                             "TAX_CD"       => VARS::post("TAX_CD"),        //コード
                             "DATE_FROM"    => str_replace("/", "-", VARS::post("DATE_FROM")),     //開始日
                             "DATE_TO"      => str_replace("/", "-", VARS::post("DATE_TO")),       //終了日
                             "TAX_VALUE"    => VARS::post("TAX_VALUE"),     //値
                             "TAX_SUMMARY"  => VARS::post("TAX_SUMMARY")    //内容
                             );

    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if(strlen($this->field["TAX_CD"]) == 0){
            $this->setWarning("MSG301","(コード：名称)");
            return false;
        }
        if(strlen($this->field["DATE_FROM"]) == 0){
            $this->setWarning("MSG301","(開始日)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["TAX_VALUE"]) > 10){
            $this->setWarning("MSG915","(値は10バイトまでです。)");
            return false;
        }
        if (strlen($this->field["TAX_SUMMARY"]) > 30){
            $this->setWarning("MSG915","(内容は30バイトまでです。)");
            return false;
        }

        if(($kind=="update" || $kind=="delete") && ($this->taxCd !== $this->field["TAX_CD"] || $this->dateFrom !== $this->field["DATE_FROM"])){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $db = Query::dbCheckOut();
        $row = knjp987Query::getRow($this, $this->field["TAX_CD"], $this->field["DATE_FROM"]);
        Query::dbCheckIn($db);

        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($kind == "update" || $kind == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if($this->validateInput("insert")){
            knjp987Query::getInsertQuery($this, $this->field);
            $this->taxCd    = $this->field["TAX_CD"];
            $this->dateFrom = $this->field["DATE_FROM"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if($this->validateInput("update")){
            knjp987Query::getUpdateQuery($this, $this->field);
            $this->taxCd    = $this->field["TAX_CD"];
            $this->dateFrom = $this->field["DATE_FROM"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if($this->validateInput("delete")){
            knjp987Query::getDeleteQuery($this, $this->field);
            $this->taxCd    = "";
            $this->dateFrom = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
