<?php
//定数定義
class knjl401hModel extends Model
{
    public $field      = array();
    public $cmd;
    public $examyear;
    public $applicantdiv;      //primary
    public $exam_type;         //primary
    public $year;              //入試年度
    public $NextYear;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->exam_type);
        }

        $this->examyear = CTRL_YEAR + 1;

        if (VARS::request("year") != "") {
            $this->year = VARS::request("year");
        }
        $this->year = ($this->year == "") ? $this->examyear : $this->year;

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }

        if (VARS::get("EXAM_TYPE") != "") {
            $this->exam_type = VARS::get("EXAM_TYPE");
        }

        $this->field = array("EXAM_TYPE"     =>  VARS::post("EXAM_TYPE"),     //受験型
                             "EXAMTYPE_NAME" =>  VARS::post("EXAMTYPE_NAME"), //受験型名
                             "TEST_DIV"      =>  VARS::post("TEST_DIV"),      //入試回数
                             "MAPPING_NAME"  =>  VARS::post("MAPPING_NAME")   //認識用文字列
                             );

        $this->cmd = VARS::request("cmd");

        //入試制度が初期値でセットされていない場合
        if ($this->applicantdiv == "") {
            $db = Query::dbCheckOut();
            //入試制度の初期値取得
            $appNamespare       = $db->getOne(knjl401hQuery::getNameSpare2($this, "L003"));
            $this->applicantdiv = $db->getOne(knjl401hQuery::getNameMstSet($this, "L003", $appNamespare));
            Query::dbCheckIn($db);
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->applicantdiv) == 0) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["EXAM_TYPE"]) == 0) {
            $this->setWarning("MSG301", "( 受験型コード )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["EXAMTYPE_NAME"]) == 0) {
            $this->setWarning("MSG301", "( 受験型 )");
            return false;
        }
        //キー値が変更されているかをチェック
        if (($this->field["EXAM_TYPE"] != $this->exam_type) && ($kind == "update" || $kind == "delete")) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $query = knjl401hQuery::getRow($this->year, $this->applicantdiv, $this->field["EXAM_TYPE"]);
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
            $this->setWarning("MSG915", "( 受験型は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["MAPPING_NAME"]) > 60) {
            $this->setWarning("MSG915", "( 認識用文字列は60バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl401hQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl401hQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl401hQuery::getDeleteQuery($this);
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
        if (!checkThisYear($db, $this->year)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl401hQuery::getCopyQuery($this->year, $this->nextYear);
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
    $query = knjl401hQuery::getCheckExamTypeMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear)
{
    $query = knjl401hQuery::getCheckExamTypeMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
