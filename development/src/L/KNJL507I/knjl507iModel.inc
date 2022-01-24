<?php

require_once('for_php7.php');

//定数定義
class knjl507iModel extends Model
{
    public $field      = array();
    public $cmd;
    public $examyear;
    public $year;              //primary 入試年度
    public $applicantdiv;      //primary
    public $testdiv;           //primary
    public $generalDiv;        //primary
    public $generalCd;         //primary
    public $NextYear;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->generalDiv);
            unset($this->generalCd);
        }

        $this->examyear = CTRL_YEAR + 1;

        if (VARS::request("year") != "") {
            $this->year = VARS::request("year");
        }
        $this->year = ($this->year == "") ? $this->examyear : $this->year;

        $this->applicantdiv = "2";  // '2'固定
        $this->testdiv      = "0";  // '0'固定
        $this->generalDiv   = "03"; // '03'：判定マーク　固定

        if (VARS::get("GENERAL_CD") != "") {
            $this->generalCd = VARS::get("GENERAL_CD");
        }

        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);

        $this->field = array("ENTEXAMYEAR"  =>  $this->year,                 //入試年度
                             "APPLICANTDIV" =>  $this->applicantdiv,         //入試制度
                             "TESTDIV"      =>  $this->testdiv,              //入試区分
                             "GENERAL_DIV"  =>  $this->generalDiv,           //DIV
                             "GENERAL_CD"   =>  VARS::post("GENERAL_CD"),    //判定マークコード
                             "GENERAL_NAME" =>  VARS::post("GENERAL_NAME"),  //判定マーク名称
                             "GENERAL_ABBV" =>  VARS::post("GENERAL_ABBV"),  //判定マーク略称
                             "GENERAL_MARK" =>  VARS::post("GENERAL_MARK"),  //判定マーク
                             "REMARK1"      =>  VARS::post("REMARK1")        //対応コースコード
                             );

        $this->cmd = VARS::request("cmd");
    }

    //指定されたフィールド値が半角英数字（空白、記号を除く）として設定されているかをチェックする
    private function validateHalfCharOnly($val)
    {
        if (preg_match('/^([0-9]|[A-Z]|[a-z])+$/', $val) == false) {
            return false;
        }
        return true;
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["GENERAL_CD"]) == 0) {
            $this->setWarning("MSG301", "( 判定マークコード )");
            return false;
        }
        //空文字チェック
        if (($kind=="insert" || $kind=="update") && strlen($this->field["GENERAL_MARK"]) == 0) {
            $this->setWarning("MSG301", "( 判定マーク )");
            return false;
        }
        //空文字チェック
        if (($kind=="insert" || $kind=="update") && strlen($this->field["GENERAL_NAME"]) == 0) {
            $this->setWarning("MSG907", "( 判定マーク名称 )");
            return false;
        }

        //文字種チェック
        if ($this->validateHalfCharOnly($this->field["GENERAL_CD"]) == false) {
            $this->setWarning("MSG905", "( 判定マークコード )");
            return false;
        }

        //キー値が変更されているかをチェック
        if ($kind=="update" || $kind == "delete") {
            if ($this->field["GENERAL_CD"] != $this->generalCd) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //１レコード取得
        $query = knjl507iQuery::getRow($this, $this->field["GENERAL_CD"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "( 判定マークコード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        Query::dbCheckIn($db);

        //文字数チェック
        if (strlen($this->field["GENERAL_MARK"]) > 9) {
            $this->setWarning("MSG915", "( 判定マークは9バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["GENERAL_NAME"]) > 30) {
            $this->setWarning("MSG915", "( 判定マーク名称は30バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["GENERAL_ABBV"]) > 15) {
            $this->setWarning("MSG915", "( 判定マーク略称は15バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl507iQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl507iQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->examcoursecd = $this->field["EXAMCOURSECD"];
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl507iQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
            $this->field = array();
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
        if (!checkThisYear($db, $this->year, $this->generalDiv)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this->nextYear, $this->generalDiv)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl507iQuery::getCopyQuery($this, $this->nextYear);
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

function checkThisYear($db, $year, $generalDiv)
{
    $query = knjl507iQuery::getCheckGeneralMstSql($year, $generalDiv);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear, $generalDiv)
{
    $query = knjl507iQuery::getCheckGeneralMstSql($nextYear, $generalDiv);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
