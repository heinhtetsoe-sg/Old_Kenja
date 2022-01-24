<?php

require_once('for_php7.php');

//定数定義
class knjl509iModel extends Model
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
        $this->generalDiv   = "05"; // '05'：特待理由　固定

        if (VARS::get("GENERAL_CD") != "") {
            $this->generalCd = VARS::get("GENERAL_CD");
        }

        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);

        $this->field = array("ENTEXAMYEAR"  =>  $this->year,                 //入試年度
                             "APPLICANTDIV" =>  $this->applicantdiv,         //入試制度
                             "TESTDIV"      =>  $this->testdiv,              //入試区分
                             "GENERAL_DIV"  =>  $this->generalDiv,           //DIV
                             "GENERAL_CD"   =>  VARS::post("GENERAL_CD"),    //特待理由コード
                             "GENERAL_NAME" =>  VARS::post("GENERAL_NAME"),  //特待理由名称
                             "GENERAL_MARK" =>  VARS::post("GENERAL_MARK"),  //特待理由記号
                             "REMARK1"      =>  VARS::post("REMARK1")        //入学者確約書
                             );

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["GENERAL_CD"]) == 0) {
            $this->setWarning("MSG301", "( 特待理由コード )");
            return false;
        }
        //空文字チェック
        if (($kind=="insert" || $kind=="update") && strlen($this->field["GENERAL_NAME"]) == 0) {
            $this->setWarning("MSG301", "( 特待理由名称 )");
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
        $query = knjl509iQuery::getRow($this, $this->field["GENERAL_CD"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "( 特待理由コード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        Query::dbCheckIn($db);

        //文字数チェック
        if (strlen($this->field["GENERAL_NAME"]) > 30) {
            $this->setWarning("MSG915", "( 特待名称は30バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["GENERAL_MARK"]) > 9) {
            $this->setWarning("MSG915", "( 特待記号は9バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl509iQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl509iQuery::getInsertQuery($this);
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
            knjl509iQuery::getDeleteQuery($this);
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
            $query = knjl509iQuery::getCopyQuery($this, $this->nextYear);
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
    $query = knjl509iQuery::getCheckGeneralMstSql($year, $generalDiv);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear, $generalDiv)
{
    $query = knjl509iQuery::getCheckGeneralMstSql($nextYear, $generalDiv);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
