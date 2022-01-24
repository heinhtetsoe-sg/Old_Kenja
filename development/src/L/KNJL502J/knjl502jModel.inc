<?php

require_once('for_php7.php');

//定数定義
class knjl502jModel extends Model {
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

        $this->year = CTRL_YEAR + 1;

        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::get("EXAM_TYPE");
        if (isset($temp)) {
            $this->examType = VARS::get("EXAM_TYPE");
        }
        $temp = VARS::get("EXAMTYPE_NAME");
        if (isset($temp)) {
            $this->examtypeName = VARS::get("EXAMTYPE_NAME");
        }
        $temp = VARS::request("EXAMTYPE_NAME_ABBV");
        if (isset($temp)) {
            $this->examtypeNameAbbv = VARS::request("EXAMTYPE_NAME_ABBV");
        }
        $temp = VARS::request("SUBCLASSCD");
        if (isset($temp)) {
            $this->subclasscd = VARS::request("SUBCLASSCD");
        }
        $temp = VARS::request("SUBCLASS_SELECT");
        if (isset($temp)) {
            $this->subclassSelect = VARS::request("SUBCLASS_SELECT");
        }
        $temp = VARS::request("JUDGE_SUMMARY");
        if (isset($temp)) {
            $this->judgeSummary = VARS::request("JUDGE_SUMMARY");
        }

        $this->field = array("ENTEXAMYEAR"          =>  $this->year,                        //入試年度
                             "APPLICANTDIV"         =>  $this->applicantdiv,                //学校種別
                             "EXAM_TYPE"            =>  VARS::post("EXAM_TYPE"),            //入試方式CD
                             "EXAMTYPE_NAME"        =>  VARS::post("EXAMTYPE_NAME"),        //入試方式名称
                             "EXAMTYPE_NAME_ABBV"   =>  VARS::post("EXAMTYPE_NAME_ABBV"),   //帳票出力略称
                             "SUBCLASSCD"           =>  VARS::post("SUBCLASSCD"),           //科目
                             "SUBCLASS_SELECT"      =>  VARS::post("SUBCLASS_SELECT"),      //科目選択
                             "JUDGE_SUMMARY"        =>  VARS::post("JUDGE_SUMMARY")         //判定集計
                             );

        //入試科目取得
        $db = Query::dbCheckOut();
        //科目選択
        $this->subClassArr = array();
        $query  = knjl502jQuery::getNameMst($this, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->subClassArr[$row["NAMECD2"]] = $row["NAME1"];
            //$this->field[$row["NAMECD2"]] = VARS::post($row["NAMECD2"]);
            $this->field["SUBCLASSCD_".$row["NAMECD2"]] = $row["NAMECD2"];
            $this->field["SUBCLASS_SELECT_".$row["NAMECD2"]] = VARS::post("SUBCLASS_SELECT_".$row["NAMECD2"]);
            $this->field["JUDGE_SUMMARY_".$row["NAMECD2"]] = VARS::post("JUDGE_SUMMARY_".$row["NAMECD2"]);
        }
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301","( 学校種別 )");
            return false;
        }
        if (strlen($this->field["EXAM_TYPE"]) == 0) {
            $this->setWarning("MSG301","( 入試方式CD )");
            return false;
        }
        if (strlen($this->field["EXAMTYPE_NAME"]) == 0) {
            $this->setWarning("MSG301","( 入試方式名称 )");
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
        $query = knjl502jQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["EXAM_TYPE"]);
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
            $this->setWarning("MSG915","( 入試方式名称は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["EXAMTYPE_NAME_ABBV"]) > 6) {
            $this->setWarning("MSG915","( 帳票出力略称は6バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl502jQuery::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl502jQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        $this->examType = $this->field["EXAM_TYPE"];
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl502jQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = array();
        }
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
