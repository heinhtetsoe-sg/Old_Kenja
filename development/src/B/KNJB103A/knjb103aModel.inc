<?php
//定数定義
class knjb103aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $examyear;
    public $year;              //入試年度
    public $NextYear;
    public $lastPeriodCd;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->lastPeriodCd);
        }

        $this->examyear = CTRL_YEAR;

        if (VARS::request("year") != "") {
            $this->year = VARS::request("year");
        }
        $this->year = ($this->year == "") ? CTRL_YEAR : $this->year;

        $this->field = array("SEMESTER"      =>  VARS::get("SEMESTER")   != "" ? VARS::get("SEMESTER")   : VARS::post("SEMESTER"),         //学期
                             "GRADE"         =>  VARS::get("GRADE")      != "" ? VARS::get("GRADE")      : VARS::post("GRADE"),            //学年
                             "SUB_TESTCD"    =>  VARS::get("SUB_TESTCD") != "" ? VARS::get("SUB_TESTCD") : VARS::post("SUB_TESTCD"),       //考査
                             "PERIODNAME2"   =>  VARS::post("PERIODNAME2"),      //時限
                             "STARTTIME_HOUR"=>  VARS::post("STARTTIME_HOUR"),   //時間
                             "STARTTIME_MINUTE"=>VARS::post("STARTTIME_MINUTE"), //分
                             "ENDTIME_HOUR"  =>  VARS::post("ENDTIME_HOUR"),     //終わり時間
                             "ENDTIME_MINUTE"=>  VARS::post("ENDTIME_MINUTE")    //終わり分
                             );

        $temp = VARS::get("PERIODCD");
        if (isset($temp)) {
            $this->lastPeriodCd = VARS::get("PERIODCD");
        }
        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["GRADE"]) == "") {
            $this->setWarning("MSG301", "( 学年 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["SEMESTER"]) == "") {
            $this->setWarning("MSG301", "( 学期 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["SUB_TESTCD"]) == "") {
            $this->setWarning("MSG301", "( 考査 )");
            return false;
        }

        //フォーム2空文字チェック
        if (strlen($this->field["PERIODNAME2"]) == "") {
            $this->setWarning("MSG301", "( 時限 )");
            return false;
        }
        //フォーム2空文字チェック
        if (strlen($this->field["STARTTIME_HOUR"]) == "") {
            $this->setWarning("MSG301", "( 開始時間[時] )");
            return false;
        }

        //フォーム2空文字チェック
        if (strlen($this->field["STARTTIME_MINUTE"]) == "") {
            $this->setWarning("MSG301", "( 開始時間[分] )");
            return false;
        }

        //フォーム2空文字チェック
        if (strlen($this->field["ENDTIME_HOUR"]) == "") {
            $this->setWarning("MSG301", "( 終了時間[時] )");
            return false;
        }

        //フォーム2空文字チェック
        if (strlen($this->field["ENDTIME_MINUTE"]) == "") {
            $this->setWarning("MSG301", "( 終了時間[分] )");
        }

        //キー値が変更されているかをチェック
        if ($this->lastPeriodCd != $this->field["PERIODNAME2"] && ($kind == "update" || $kind == "delete")) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $query = knjb103aQuery::getRow($this);
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
            knjb103aQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjb103aQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->lastPeriodCd = $this->field["PERIODNAME2"];
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjb103aQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
            $this->field["PERIODNAME2"] = null;
            $this->field["STARTTIME_HOUR"] = null;
            $this->field["STARTTIME_MINUTE"] = null;
            $this->field["ENDTIME_HOUR"] = null;
            $this->field["ENDTIME_MINUTE"] = null;
            $this->lastPeriodCd = null;
        }
        return;
    }

    //次年度のデータを作成
    public function &getCopyYearModel()
    {
        $db = Query::dbCheckOut();

        $this->beforeYear = $this->year - 1;
        $message = '';
        $flg = true;
        if (!checkBeforeYear($db, $this->beforeYear)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNowYear($db, $this->year)) {
            $message = $message . '既に当年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjb103aQuery::getCopyQuery($this->beforeYear, $this->year);
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

function checkBeforeYear($db, $year)
{
    $query = knjb103aQuery::getCheckSchTesttimeDatSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNowYear($db, $nextYear)
{
    $query = knjb103aQuery::getCheckSchTesttimeDatSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
