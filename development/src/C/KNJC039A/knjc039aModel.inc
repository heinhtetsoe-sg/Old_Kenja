<?php

require_once('for_php7.php');

//定数定義
class knjc039aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $school_kind;

    function knjc039aModel() {}

    function init() {
        common::GetControlMaster_Fnc($this->control);

        $this->cmd        = VARS::request("cmd");
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->collection_cd);
            unset($this->school_kind);
            unset($this->collection_name);
            unset($this->from_date);
            unset($this->to_date);
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd  = VARS::request("cmd");

        if (VARS::get("YEAR")) {
            $this->year   = VARS::get("YEAR");
        }
        if (VARS::get("COLLECTION_CD")) {
            $this->collection_cd   = VARS::get("COLLECTION_CD");
        }
        if (VARS::get("SCHOOL_KIND")) {
            $this->school_kind     = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("COLLECTION_NAME")) {
            $this->collection_name     = VARS::get("COLLECTION_NAME");
        }
        if (VARS::get("FROM_DATE")) {
            $this->from_date     = VARS::get("FROM_DATE");
        }
        if (VARS::get("TO_DATE")) {
            $this->to_date     = VARS::get("TO_DATE");
        }

        $this->field = array(
                             "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),
                             "COLLECTION_CD"        =>  VARS::post("COLLECTION_CD"),
                             "COLLECTION_NAME"      =>  VARS::post("COLLECTION_NAME"),
                             "FROM_DATE"            =>  VARS::post("FROM_DATE"),
                             "TO_DATE"              =>  VARS::post("TO_DATE"),
                             "SEMESTER"             =>  VARS::post("SEMESTER"),
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             "YEAR"                 =>  CTRL_YEAR,
                             );
    }

    //入力項目チェック
    function validateInput($kind) {
        //１レコード取得
        $db = Query::dbCheckOut();
        $Row = knjc039aQuery::getRow($this, $db);
        Query::dbCheckIn($db);

        //必須項目入力チェック
        if (trim($this->field["SCHOOL_KIND"]) == "" ) {
            $this->setWarning("MSG301", "(校種)");
            return false;
        }

        if (trim($this->field["COLLECTION_CD"]) == "" ) {
            $this->setWarning("MSG301", "(集計コード)");
            return false;
        }

        if (trim($this->field["COLLECTION_NAME"]) == "" && $this->cmd != "delete") {
            $this->setWarning("MSG301", "(集計単位名)");
            return false;
        }
        if ((trim($this->field["FROM_DATE"]) == "" || trim($this->field["TO_DATE"]) == "") && $this->cmd != "delete") {
            $this->setWarning('MSG301', "(集計単位)");
            return false;
        }

        //入力値の妥当性チェック
        if($this->field["COLLECTION_CD"] <= 0 && ($this->cmd == "add" || $this->cmd == "update" )){
            $this->setWarning("MSG901", "集計コードは1以上の値を入力してください。");
            return false;
        }

        if(strlen($this->field["COLLECTION_NAME"]) > 15 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG915', "集計単位名は全角5文字までです。");
            return false;
        }

        //ゼロ埋め
        $this->field["COLLECTION_CD"] = sprintf("%02d", $this->field["COLLECTION_CD"]);

        //日付逆転チェック
        if ($this->field["TO_DATE"] < $this->field["FROM_DATE"] && $this->cmd != "delete") {
            $this->setWarning('MSG916',"日付の前後関係が正しくありません。");
            return false;
        }

        //日付が対象年度内かチェック
        if((strtotime($this->field["FROM_DATE"]) < strtotime($this->year."/04/01") || strtotime($this->field["TO_DATE"]) > strtotime(($this->year+1)."/03/31")) && $kind != "delete") {
            $this->setWarning("MSG916","対象年度内の日付ではありません。");
            return false;
        }

        if ($this->cmd == "update" || $kind == "delete") {
            if ($this->school_kind != $this->field["SCHOOL_KIND"]) {
                $this->setWarning("MSG308","(校種)");
                return false;
            }
            if ($this->collection_cd != $this->field["COLLECTION_CD"]) {
                $this->setWarning("MSG308","(集計コード)");
                return false;
            }
        }

        $ret_val = knjc039aQuery::getExists($this);
        if ($this->cmd == "add") {
            if ($ret_val) {
                $this->setWarning('MSG302',"(校種)(集計コード)");
                return false;
            }
        }

        $ret_val = knjc039aQuery::getExistsAttendRemark($this);
        if($this->cmd == "delete") {
            if ($ret_val) {
                $this->setWarning('MSG203',"出欠備考が存在しています。");
                return false;
            }
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $ret_val = knjc039aQuery::getExistsPreYear($this);
        if (!$ret_val) {
            $this->setMessage("MSG203", "前年度に対象データがありません。");
            return false;
        }
        $ret_val = knjc039aQuery::getExistsThisYear($this);
        if ($ret_val) {
            $this->setMessage("MSG203", "対象年度にデータが存在します。");
            return false;
        }

        $this->copy();
        $this->setMessage('MSG201');
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->setMessage('MSG201');
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
            $this->setMessage('MSG201');
        }
        return;
    }
    //COPY時の内部メソッド
    function &copy() {
        $result = knjc039aQuery::getCopyQuery($this);
    }

    //DELETE時の内部メソッド
    function &delete() {
        knjc039aQuery::getDeleteQuery($this);
        knjc039aQuery::getDeleteAttendRemarkQuery($this);
        unset($this->school_kind);
        unset($this->collection_cd);
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert() {

        knjc039aQuery::getInsertUpdateQuery($this);
        $this->school_kind = $this->field["SCHOOL_KIND"];
        $this->collection_cd = $this->field["COLLECTION_CD"];
    }

}
?>
