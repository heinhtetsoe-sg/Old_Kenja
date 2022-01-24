<?php

require_once('for_php7.php');

//定数定義
class knjl001aModel extends Model
{
    public $field = array();
    public $cmd;
    public $applicantdiv;      //primary
    public $testdiv;           //primary
    public $year;              //入試年度
    public $NextYear;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
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
                             "APPLICANTDIV"     =>  $this->applicantdiv,            //受験校種
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //試験CD
                             "TESTDIV_NAME"     =>  VARS::post("TESTDIV_NAME"),     //試験名称
                             "TESTDIV_ABBV"     =>  VARS::post("TESTDIV_ABBV"),     //試験略称
                             "TEST_DATE1"       =>  VARS::post("TEST_DATE1"),       //試験日
                             "PRETEST_FLG"      =>  VARS::post("PRETEST_FLG"),      //プレテスト
                             "DEFAULT_FLG"      =>  VARS::post("DEFAULT_FLG")       //初期値
                             );

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if (strlen($this->field["TESTDIV"]) == 0) {
            $this->setWarning("MSG301", "( 試験CD )");
            return false;
        }
        if (strlen($this->field["TESTDIV_NAME"]) == 0) {
            $this->setWarning("MSG301", "( 試験名称 )");
            return false;
        }
        if (strlen($this->field["TESTDIV_ABBV"]) == 0) {
            $this->setWarning("MSG301", "( 試験略称 )");
            return false;
        }
        if (strlen($this->field["TEST_DATE1"]) == 0) {
            $this->setWarning("MSG301", "( 試験日 )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["TESTDIV"] != $this->testdiv) &&
            ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl001aQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["TESTDIV"]);
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
        if (strlen($this->field["TESTDIV_NAME"]) > 60) {
            $this->setWarning("MSG915", "( 試験名称は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["TESTDIV_ABBV"]) > 30) {
            $this->setWarning("MSG915", "( 試験略称は30バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl001aQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl001aQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        $this->testdiv = $this->field["TESTDIV"];
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl001aQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    public function &getCopyYearModel()
    {
        $this->nextYear = $this->year + 1;

        //今年度のデータが存在するか確認
        $t_year = knjl001aQuery::checkData($this->year);
        if (!isset($t_year)) {
            $this->setWarning('MSG203', "今年度のデータが存在しません。");
            return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjl001aQuery::checkData($this->nextYear);
        if (isset($pre_year)) {
            $this->setWarning('MSG203', "既に次年度のデータが作成されています。");
            return false;
        }

        knjl001aQuery::getCopyQuery($this->year, $this->nextYear);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
?>
