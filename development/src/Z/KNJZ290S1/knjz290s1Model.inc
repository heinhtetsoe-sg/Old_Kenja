<?php

require_once('for_php7.php');


class knjz290s1Model extends Model {
    var $field = array();
    var $getField = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjz290s1Model()
    {
    }
    function init()
    {

        $this->cmd = VARS::request("cmd");

        if ($this->cmd != "reset") {
            $this->field = array("SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),
                                 "FROM_DATE"    =>  VARS::post("FROM_DATE"),
                                 "TO_DATE"      =>  VARS::post("TO_DATE"),
                                 "STAFFCD"      =>  VARS::post("STAFFCD"),
                                 );
        }

        if (VARS::get("STAFFCD")){
            $this->getField = array("SCHOOL_KIND"  =>  VARS::get("SCHOOL_KIND"),
                                    "FROM_DATE"    =>  VARS::get("FROM_DATE"),
                                    "TO_DATE"      =>  VARS::get("TO_DATE"),
                                    "STAFFCD"      =>  VARS::get("STAFFCD"),
                                    );
            $this->field = array("SCHOOL_KIND"  =>  VARS::get("SCHOOL_KIND"),
                                 "FROM_DATE"    =>  VARS::get("FROM_DATE"),
                                 "TO_DATE"      =>  VARS::get("TO_DATE"),
                                 "STAFFCD"      =>  VARS::get("STAFFCD"),
                                 );
        }

        if ($this->cmd == "") {
            unset($this->field);
            unset($this->getField);
        }
        //プロパティファイル -- prgInfo.properties
        $this->getPropertiesAll();
    }

   //入力項目チェック
    function validateInput()
    {
        if (trim($this->field["SCHOOL_KIND"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(校種)");
            return false;
        }
        //キーのチェック
        if (trim($this->field["STAFFCD"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(職員コード)");
            return false;
        }
        if (trim($this->field["FROM_DATE"]) == "" && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning('MSG901',"(開始日)");
            return false;
        }

        if (($this->cmd == "update" || $this->cmd == "delete") && $this->getField["SCHOOL_KIND"] != $this->field["SCHOOL_KIND"]) {
            $this->setWarning("更新/削除では、校種の変更は出来ません。");
            return false;
        }

        if ($this->cmd == "delete" && $this->getField["FROM_DATE"] != $this->field["FROM_DATE"]) {
            $this->setWarning("削除では、開始日の変更は出来ません。");
            return false;
        }

        $db = Query::dbCheckOut();

        //１レコード取得
        $query = knjz290s1Query::getDispData($this->field);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($this->cmd == "delete" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918');
            return false;
        }

        //一つ前のデータを取得
        $query = knjz290s1Query::getLastDispData($this->field);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $row["FROM_DATE"] = str_replace("-", "/", $row["FROM_DATE"]);

        if ($this->cmd == "update" && $row["FROM_DATE"] >= $this->field["FROM_DATE"]) {
            $this->setWarning('MSG913', "開始日付を{$row["FROM_DATE"]}より大きくして下さい。");
            return false;
        }

        //一つ前のデータを取得
        $query = knjz290s1Query::getMaxDispData($this->field);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $row["FROM_DATE"] = str_replace("-", "/", $row["FROM_DATE"]);

        if ($this->cmd == "add" && $row["FROM_DATE"] >= $this->field["FROM_DATE"]) {
            $this->setWarning('MSG913', "開始日付を{$row["FROM_DATE"]}より大きくして下さい。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjz290s1Query::getInsertQuery($this->field);
            $this->setMessage('MSG201');
            $this->getField = $this->field;
        }
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjz290s1Query::getUpdateQuery($this->field);
            $this->setMessage('MSG201');
            $this->getField = $this->field;
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjz290s1Query::getDeleteQuery($this->field);
            $this->setMessage('MSG201');
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
