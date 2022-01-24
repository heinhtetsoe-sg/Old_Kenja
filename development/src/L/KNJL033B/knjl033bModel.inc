<?php

require_once('for_php7.php');

//定数定義
class knjl033bModel extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $applicantdiv;      //primary
    var $judge_kind;        //primary

    function knjl033bModel() {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->judge_kind);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"          =>  $this->year,                        //入試年度
                             "APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "JUDGE_KIND"           =>  VARS::post("JUDGE_KIND"),           //奨学金種別
                             "ENT_MONEY"            =>  VARS::post("ENT_MONEY"),            //入学金
                             "ENT_MONEY_NAME"       =>  VARS::post("ENT_MONEY_NAME"),       //入学金名称
                             "FAC_MONEY"            =>  VARS::post("FAC_MONEY"),            //施設拡充費
                             "FAC_MONEY_NAME"       =>  VARS::post("FAC_MONEY_NAME"),       //施設拡充費名称
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("APPLICANTDIV");
        if (isset($temp)){
            $this->applicantdiv = VARS::get("APPLICANTDIV");
        }
        $temp = VARS::get("JUDGE_KIND");
        if (isset($temp)){
            $this->judge_kind = VARS::get("JUDGE_KIND");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0){
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["JUDGE_KIND"]) == 0){
            $this->setWarning("MSG301","( 奨学金種別 )");
            return false;
        }
        //キー値が変更されているかをチェック
        if (($this->field["APPLICANTDIV"] != $this->applicantdiv ||
             $this->field["JUDGE_KIND"]   != $this->judge_kind ) && 
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $query = knjl033bQuery::getRow($this->year, $this->field["APPLICANTDIV"], $this->field["JUDGE_KIND"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["ENT_MONEY_NAME"]) > 60){
            $this->setWarning("MSG915","( 入学金名称は60バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["FAC_MONEY_NAME"]) > 60){
            $this->setWarning("MSG915","( 施設拡充費名称は60バイトまでです。)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjl033bQuery::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjl033bQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjl033bQuery::getDeleteQuery($this, $this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel()
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
            $query = knjl033bQuery::getCopyQuery($this->year, $this->nextYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }
        Query::dbCheckIn($db);
    }

    //変数をクリアします
    function clean()
    {
    }
}

function checkThisYear($db, $year) {
    $query = knjl033bQuery::getCheckPaymentMoneyYmstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjl033bQuery::getCheckPaymentMoneyYmstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
