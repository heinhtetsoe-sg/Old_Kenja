<?php

require_once('for_php7.php');

//定数定義
class knjl501hModel extends Model {
    var $field = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $testdiv;           //primary
    var $year;              //入試年度
    var $NextYear;

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::get("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::get("TESTDIV");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                    //入試年度
                             "APPLICANTDIV"     =>  $this->applicantdiv,            //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別CD
                             "TESTDIV_NAME"     =>  VARS::post("TESTDIV_NAME")      //入試種別名称
                             );

        $this->cmd = VARS::request("cmd");

    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301","( 学校種別 )");
            return false;
        }
        if (strlen($this->field["TESTDIV"]) == 0) {
            $this->setWarning("MSG301","( 入試種別CD )");
            return false;
        }
        if (strlen($this->field["TESTDIV_NAME"]) == 0) {
            $this->setWarning("MSG301","( 入試種別名称 )");
            return false;
        }

        //ゼロ埋め
        $this->field["TESTDIV"] = sprintf("%02d", $this->field["TESTDIV"]);

        //キー値が変更されているかをチェック
        if (($this->field["TESTDIV"] != $this->testdiv ) && 
            ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl501hQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["TESTDIV"]);
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
        if (strlen($this->field["TESTDIV_NAME"]) > 30) {
            $this->setWarning("MSG915","( 入試種別名称は30バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl501hQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl501hQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        $this->testdiv = $this->field["TESTDIV"];
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl501hQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    function &getCopyYearModel() {
        $this->nextYear = $this->year + 1;

        //今年度のデータが存在するか確認
        $t_year = knjl501hQuery::CheckData($this->year);
        if (!isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在しません。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjl501hQuery::CheckData($this->nextYear);
        if (isset($pre_year)) {
           $this->setWarning('MSG203', "既に次年度のデータが作成されています。");
           return false;
        }

        knjl501hQuery::getCopyQuery($this->year, $this->nextYear);
        $this->setMessage("MSG201");
        return;

    }

    //変数をクリアします
    function clean() {
    }
}
?>
