<?php

require_once('for_php7.php');

//定数定義
class knjl002aModel extends Model {
    var $field = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $examType;          //primary
    var $year;              //入試年度
    var $NextYear;

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
        }

        $this->year = ($this->year == "") ? CTRL_YEAR + 1: $this->year;

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::get("EXAM_TYPE");
        if (isset($temp)) {
            $this->examType = VARS::get("EXAM_TYPE");
        }

        $this->field = array("ENTEXAMYEAR"          =>  $this->year,                        //入試年度
                             "APPLICANTDIV"         =>  $this->applicantdiv,                //受験校種
                             "EXAM_TYPE"            =>  VARS::post("EXAM_TYPE"),            //受験型CD
                             "EXAMTYPE_NAME"        =>  VARS::post("EXAMTYPE_NAME"),        //受験型名称
                             "EXAMTYPE_NAME_ABBV"   =>  VARS::post("EXAMTYPE_NAME_ABBV")    //受験型略称
                             );

        //受験科目取得
        $db = Query::dbCheckOut();
        $this->subClassArr = array();
        $query  = knjl002aQuery::getNameMst($this, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD2"] == "6") continue; //面接は除く
            $this->subClassArr["SUBCLASS_".$row["NAMECD2"]] = $row["NAME1"];
            $this->field["SUBCLASS_".$row["NAMECD2"]] = VARS::post("SUBCLASS_".$row["NAMECD2"]);
        }
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301","( 受験校種 )");
            return false;
        }
        if (strlen($this->field["EXAM_TYPE"]) == 0) {
            $this->setWarning("MSG301","( 受験型CD )");
            return false;
        }
        if (strlen($this->field["EXAMTYPE_NAME"]) == 0) {
            $this->setWarning("MSG301","( 受験型名称 )");
            return false;
        }
        if (strlen($this->field["EXAMTYPE_NAME_ABBV"]) == 0) {
            $this->setWarning("MSG301","( 受験型略称 )");
            return false;
        }

        //ゼロ埋め
        $this->field["EXAM_TYPE"] = sprintf("%02d", $this->field["EXAM_TYPE"]);

        //キー値が変更されているかをチェック
        if (($this->field["EXAM_TYPE"] != $this->examType) && 
            ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl002aQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["EXAM_TYPE"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["EXAMTYPE_NAME"]) > 60) {
            $this->setWarning("MSG915","( 受験型名称は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["EXAMTYPE_NAME_ABBV"]) > 15) {
            $this->setWarning("MSG915","( 受験型略称は15バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl002aQuery::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl002aQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        $this->examType = $this->field["EXAM_TYPE"];
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl002aQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    function &getCopyYearModel() {
        $this->nextYear = $this->year + 1;

        //今年度のデータが存在するか確認
        $t_year = knjl002aQuery::CheckData($this->year);
        if (!isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在しません。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjl002aQuery::CheckData($this->nextYear);
        if (isset($pre_year)) {
           $this->setWarning('MSG203', "既に次年度のデータが作成されています。");
           return false;
        }

        knjl002aQuery::getCopyQuery($this->year, $this->nextYear);
        $this->setMessage("MSG201");
        return;

    }

    //変数をクリアします
    function clean() {
    }
}
?>
