<?php

require_once('for_php7.php');

//定数定義
class knjz020fModel extends Model
{
    public $field      = array();
    public $cmd;
    public $applicantdiv;      //primary
    public $testdiv;           //primary
    public $totalcd;           //primary
    public $examcoursecd;      //primary
    public $testsubclasscd;    //primary
    public $exam_type;         //primary
    public $year;              //入試年度
    public $NextYear;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->totalcd);
            unset($this->examcoursecd);
            unset($this->testsubclasscd);
            unset($this->exam_type);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::request("TESTDIV");
        }

        $this->field = array("ENTEXAMYEAR"              =>  $this->year,                    //入試年度
                             "APPLICANTDIV"             =>  $this->applicantdiv,            //入試制度
                             "TESTDIV"                  =>  $this->testdiv,                 //入試区分
                             "TOTALCD"                  =>  VARS::post("TOTALCD"),          //課程学科
                             "EXAMCOURSECD"             =>  VARS::post("EXAMCOURSECD"),     //受験コース
                             "TESTSUBCLASSCD"           =>  VARS::post("TESTSUBCLASSCD"),   //試験科目
                             "EXAM_TYPE"                =>  VARS::post("EXAM_TYPE"),        //受験型
                             "PERFECT"                  =>  VARS::post("PERFECT"),          //満点
                             "APP_HOLD"                 =>  VARS::post("APP_HOLD")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("TOTALCD");
        if (isset($temp)) {
            $this->totalcd = VARS::get("TOTALCD");
        }
        $temp = VARS::get("EXAMCOURSECD");
        if (isset($temp)) {
            $this->examcoursecd = VARS::get("EXAMCOURSECD");
        }
        $temp = VARS::get("TESTSUBCLASSCD");
        if (isset($temp)) {
            $this->testsubclasscd = VARS::get("TESTSUBCLASSCD");
        }
        $temp = VARS::get("EXAM_TYPE");
        if (isset($temp)) {
            $this->exam_type = VARS::get("EXAM_TYPE");
        }

        //入試制度と区分が初期値でセットされていない場合
        if ($this->applicantdiv == "" || $this->testdiv == "") {
            $db = Query::dbCheckOut();
            //入試制度の初期値取得
            $appNamespare       = $db->getOne(knjz020fQuery::getNameSpare2($this, "L003"));
            $this->applicantdiv = $db->getOne(knjz020fQuery::getNameMstSet($this, "L003", $appNamespare));

            //入試区分の初期値取得
            $namecd2            = $this->applicantdiv == "1" ? "L024" : "L004";
            $testNamespare      = $db->getOne(knjz020fQuery::getNameSpare2($this, $namecd2));
            $this->testdiv      = $db->getOne(knjz020fQuery::getNameMstSet($this, $namecd2, $testNamespare));
            Query::dbCheckIn($db);
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["TESTDIV"]) == 0) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["TOTALCD"]) == 0) {
            $this->setWarning("MSG301", "( 課程学科 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["EXAMCOURSECD"]) == 0) {
            $this->setWarning("MSG301", "( 受験コース )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["TESTSUBCLASSCD"]) == 0) {
            $this->setWarning("MSG301", "( 試験科目 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["EXAM_TYPE"]) == 0) {
            $this->setWarning("MSG301", "( 受験型 )");
            return false;
        }
        //空文字チェック
        if (($this->cmd == "add" || $this->cmd == "update") && strlen($this->field["PERFECT"]) == 0) {
            $this->setWarning("MSG301", "( 満点 )");
            return false;
        }
        //キー値が変更されているかをチェック
        if ((($this->field["TESTSUBCLASSCD"] != $this->testsubclasscd) || ($this->field["EXAM_TYPE"] != $this->exam_type)) &&
            ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $query = knjz020fQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->field["TOTALCD"], $this->field["EXAMCOURSECD"], $this->field["TESTSUBCLASSCD"], $this->field["EXAM_TYPE"]);
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
        if (strlen($this->field["PERFECT"]) > 3) {
            $this->setWarning("MSG915", "( 満点は3バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz020fQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjz020fQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        $this->totalcd          = $this->field["TOTALCD"];
        $this->examcoursecd     = $this->field["EXAMCOURSECD"];
        $this->testsubclasscd   = $this->field["TESTSUBCLASSCD"];
        $this->exam_type        = $this->field["EXAM_TYPE"];
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz020fQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    public function &getCopyYearModel()
    {
        $db = Query::dbCheckOut();

        $this->nextYear = $this->year + 1;
        $message = '';
        $flg = true;
        if (!checkThisYear($db, $this->year)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjz020fQuery::getCopyQuery($this->year, $this->nextYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        Query::dbCheckIn($db);
    }

    //変数をクリアします
    public function clean()
    {
    }
}

function checkThisYear($db, $year)
{
    $query = knjz020fQuery::getCheckPerfectMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear)
{
    $query = knjz020fQuery::getCheckPerfectMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
