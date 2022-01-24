<?php

require_once('for_php7.php');

//定数定義
class knjl505iModel extends Model
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
    public $subjectList;

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

        $this->subjectList   = array();
        $this->subjectList[] = array("label" => "1:普通科", "value" => "1");
        $this->subjectList[] = array("label" => "2:工業科", "value" => "2");

        if (VARS::request("year") != "") {
            $this->year = VARS::request("year");
        }
        $this->year = ($this->year == "") ? $this->examyear : $this->year;

        $this->applicantdiv = "2";  // '2'固定
        $this->testdiv      = "0";  // '0'固定
        $this->generalDiv   = "01"; // '01'：類別　固定

        if (VARS::get("GENERAL_CD") != "") {
            $this->generalCd = VARS::get("GENERAL_CD");
        }

        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);

        $this->field = array("ENTEXAMYEAR"    =>  $this->year,                    //入試年度
                             "APPLICANTDIV"   =>  $this->applicantdiv,            //入試制度
                             "TESTDIV"        =>  $this->testdiv,                 //入試区分
                             "GENERAL_DIV"    =>  $this->generalDiv,              //DIV
                             "GENERAL_CD"     =>  VARS::post("GENERAL_CD"),       //類
                             "GENERAL_NAME"   =>  VARS::post("GENERAL_NAME"),     //類名称
                             "GENERAL_ABBV"   =>  VARS::post("GENERAL_ABBV"),     //略称
                             "REMARK1"        =>  VARS::post("REMARK1"),          //学科
                             "ENTER_TOTALCD"  =>  $this->field["ENTER_TOTALCD"],  //課程学科(賢者)
                             );

        //一度POSTした値は賢者課程学科コンボ切替以外のPOSTでのみ上書き
        if (!isset($this->field["ENTER_TOTALCD"]) || $this->cmd != "chgGakkaCmg") {
            $this->field["ENTER_TOTALCD"] = VARS::post("ENTER_TOTALCD");
        }

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "chgGakkaCmg") {
            $this->cmd = "edit";
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["GENERAL_CD"]) == 0) {
            $this->setWarning("MSG301", "( 類 )");
            return false;
        }
        //空文字チェック
        if (($kind=="insert" || $kind=="update") && strlen($this->field["GENERAL_NAME"]) == 0) {
            $this->setWarning("MSG301", "( 類名称 )");
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
        $query = knjl505iQuery::getRow($this, $this->field["GENERAL_CD"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "( 類 )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //１レコード取得(02：コース)
        $query = knjl505iQuery::getExistCourceQuery($this);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //存在チェック
        if (($this->cmd == "delete") && $row["CNT"]) {
            $this->setWarning("コースマスタに登録済みです。");
            return false;
        }
        Query::dbCheckIn($db);

        //文字数チェック
        if (strlen($this->field["GENERAL_NAME"]) > 60) {
            $this->setWarning("MSG915", "( 類名称は60バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["GENERAL_ABBV"]) > 15) {
            $this->setWarning("MSG915", "( 略称は15バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl505iQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl505iQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->generalCd = $this->field["GENERAL_CD"];
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl505iQuery::getDeleteQuery($this);
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
            $query = knjl505iQuery::getCopyQuery($this, $this->nextYear);
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
    $query = knjl505iQuery::getCheckGeneralMstSql($year, $generalDiv);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear, $generalDiv)
{
    $query = knjl505iQuery::getCheckGeneralMstSql($nextYear, $generalDiv);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
