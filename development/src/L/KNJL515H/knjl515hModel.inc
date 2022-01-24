<?php

require_once('for_php7.php');

//定数定義
class knjl515hModel extends Model {
    var $field = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $testDiv;           //入試種別
    var $examType;          //入試方式
    var $distinctId;        //入試判別
    var $testDate;          //試験日
    var $year;              //入試年度
    var $NextYear;

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->distinctId);
        }

        $temp = VARS::request("YEAR");
        if (isset($temp)) {
            $this->year = VARS::request("YEAR");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::get("TESTDIV");
        if (isset($temp)) {
            $this->testDiv = VARS::get("TESTDIV");
        }
        $temp = VARS::get("EXAM_TYPE");
        if (isset($temp)) {
            $this->examType = VARS::get("EXAM_TYPE");
        }
        $temp = VARS::get("DISTINCT_ID");
        if (isset($temp)) {
            $this->distinctId = VARS::get("DISTINCT_ID");
        }
        $temp = VARS::get("TEST_DATE");
        if (isset($temp)) {
            $this->testDate = VARS::get("TEST_DATE");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                    //入試年度
                             "APPLICANTDIV"     =>  $this->applicantdiv,            //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別
                             "EXAM_TYPE"        =>  VARS::post("EXAM_TYPE"),        //入試方式
                             "TEST_DATE"        =>  VARS::post("TEST_DATE"),        //試験日
                             "DISTINCT_ID"      =>  VARS::post("DISTINCT_ID"),      //入試判別ID
                             "DISTINCT_NAME"    =>  VARS::post("DISTINCT_NAME")     //入試判別名称
                             );

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "changeCmb") {
            unset($this->distinctId);
        }

        $this->selectdata      = VARS::post("selectdata");
        $this->selectRightdata = VARS::post("selectRightdata");
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301","( 学校種別 )");
            return false;
        }
        if (strlen($this->field["TESTDIV"]) == 0) {
            $this->setWarning("MSG301","( 入試種別 )");
            return false;
        }
        if (strlen($this->field["TEST_DATE"]) == 0) {
            $this->setWarning("MSG301","( 試験日 )");
            return false;
        }
        if (strlen($this->field["DISTINCT_ID"]) == 0) {
            $this->setWarning("MSG301","( 入試判別ID )");
            return false;
        }
        if (strlen($this->field["DISTINCT_NAME"]) == 0) {
            $this->setWarning("MSG301","( 入試判別名称 )");
            return false;
        }

        //ゼロ埋め
        $this->field["DISTINCT_ID"] = sprintf("%03d", $this->field["DISTINCT_ID"]);

        //キー値が変更されているかをチェック
        if (($this->field["DISTINCT_ID"] != $this->distinctId) && ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl515hQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["DISTINCT_ID"]);
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
        if (strlen($this->field["DISTINCT_NAME"]) > 60) {
            $this->setWarning("MSG915","( 入試判別名称は60バイトまでです。)");
            return false;
        }

        //得点データがあるかチェック
        $query = knjl515hQuery::getEntexamScoreDatCnt($this);
        $db = Query::dbCheckOut();
        $scoreCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        if (($this->cmd == "update" || $this->cmd == "delete") && $scoreCnt > 0) {
            $this->setWarning("MSG203", 得点データが存在します。);
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl515hQuery::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl515hQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        $this->testDiv    = $this->field["TESTDIV"];
        $this->examType   = $this->field["EXAM_TYPE"];
        $this->testDate   = str_replace("/", "-", $this->field["TEST_DATE"]);
        $this->distinctId = $this->field["DISTINCT_ID"];
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl515hQuery::getDeleteQuery($this, $this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
