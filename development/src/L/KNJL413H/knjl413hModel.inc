<?php
//定数定義
class knjl413hModel extends Model
{
    public $field      = array();
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $examcoursecd;
    public $shdiv;
    public $year;              //入試年度
    public $NextYear;
    public $tokushinACd;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->examcoursecd);
            unset($this->shdiv);
            unset($this->hidshdiv);
        }

        $this->examyear = CTRL_YEAR + 1;

        if (VARS::request("year") != "") {
            $this->year = VARS::request("year");
        }
        $this->year = ($this->year == "") ? $this->examyear : $this->year;

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $db = Query::dbCheckOut();
        $this->applicantdiv = ($this->applicantdiv == "") ? $db->getOne(knjl413hQuery::getApplicantdivSet($this->year)) : $this->applicantdiv;
        Query::dbCheckIn($db);

        if (VARS::request("EXAMCOURSECD") != "") {
            $this->examcoursecd = VARS::request("EXAMCOURSECD");
        }

        if (VARS::request("SHDIV") != "") {
            $this->shdiv = VARS::request("SHDIV");
        }
        if (VARS::get("SHDIV") != "") {
            $this->hidshdiv = VARS::get("SHDIV");
        }

        $this->field = array("ENTEXAMYEAR"  =>  $this->year,                //入試年度
                             "APPLICANTDIV" =>  $this->applicantdiv,        //入試制度
                             "COURSECD"     =>  VARS::post("COURSECD"),     //課程コード
                             "MAJORCD"      =>  VARS::post("MAJORCD"),      //学科コード
                             "EXAMCOURSECD" =>  $this->examcoursecd,        //受験コースコード
                             "SHDIV"        =>  VARS::post("SHDIV"),        //出願区分
                             "BORDER1"      =>  VARS::post("BORDER1"),
                             "BORDER2"      =>  VARS::post("BORDER2"),
                             "BORDER3"      =>  VARS::post("BORDER3"),
                             "BORDER4"      =>  VARS::post("BORDER4"),
                             "BORDER5"      =>  VARS::post("BORDER5"),
                             "BORDER6"      =>  VARS::post("BORDER6"),
                             "BORDER7"      =>  VARS::post("BORDER7"),
                             "BORDER8"      =>  VARS::post("BORDER8"),
                             "BORDER9"      =>  VARS::post("BORDER9"),
                             "BORDER10"     =>  VARS::post("BORDER10"),
                             "BORDER11"     =>  VARS::post("BORDER11"),
                             "BORDER12"     =>  VARS::post("BORDER12"),
                             );

        $this->cmd = VARS::request("cmd");

        //受験コース 高校特進Aコースコード
        $this->tokushinACd = "02";
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
        if (strlen($this->examcoursecd) == 0) {
            $this->setWarning("MSG301", "( 受験コース )");
            return false;
        }
        //空文字チェック
        if ($this->field["SHDIV"] == "0") {
            $this->setWarning("MSG301", "( 出願区分 )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["EXAMCOURSECD"] != VARS::post("HID_EXAMCOURSECD") || $this->shdiv != $this->hidshdiv) && ($kind == "update" || $kind == "delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl413hQuery::getRow($this->year, $this->applicantdiv, $this->examcoursecd, $this->field["SHDIV"]);
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

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl413hQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl413hQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl413hQuery::getDeleteQuery($this);
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
        if (!$this->checkThisYear($db, $this->year)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!$this->checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl413hQuery::getCopyQuery($this->year, $this->nextYear);
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

    public function checkThisYear($db, $year)
    {
        $query = knjl413hQuery::getCheckExamTypeMstSql($year);
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    public function checkNextYear($db, $nextYear)
    {
        $query = knjl413hQuery::getCheckExamTypeMstSql($nextYear);
        $result = $db->getOne($query);
        if ($result == 0) {
            return true;
        }
        return false;
    }
}
