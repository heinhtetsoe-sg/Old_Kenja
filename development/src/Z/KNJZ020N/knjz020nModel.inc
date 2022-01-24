<?php

require_once('for_php7.php');

//定数定義
class knjz020nModel extends Model {
    var $field = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $testdiv;           //primary
    var $totalcd;           //primary
    var $testsubclasscd;    //primary
    var $year;              //入試年度
    var $NextYear;

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->totalcd);
            unset($this->testsubclasscd);
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

        $this->field = array("ENTEXAMYEAR"              =>  $this->year,                            //入試年度
                             "APPLICANTDIV"             =>  $this->applicantdiv,                    //入試制度
                             "TESTDIV"                  =>  $this->testdiv,                         //入試区分
                             "TOTALCD"                  =>  VARS::post("TOTALCD"),                  //受験コース
                             "TESTSUBCLASSCD"           =>  VARS::post("TESTSUBCLASSCD"),           //試験科目
                             "PERFECT"                  =>  VARS::post("PERFECT"),                  //満点
                             "APP_HOLD"                 =>  VARS::post("APP_HOLD")
                             );

        $this->cmd = VARS::request("cmd");
        
        //課程・学・科コースは0固定
        $temp = VARS::get("TOTALCD");
        if (isset($temp)){
            $this->totalcd = VARS::get("TOTALCD");
        }
        $temp = VARS::get("TESTSUBCLASSCD");
        if (isset($temp)){
            $this->testsubclasscd = VARS::get("TESTSUBCLASSCD");
        }
        
        //入試制度と区分が初期値でセットされていない場合
        if ($this->applicantdiv == "" || $this->testdiv == "") {
            $db = Query::dbCheckOut();
            $appNamespare = $db->getOne(knjz020nQuery::getNameSpare2($this, "L003"));
            $testNamespare = $db->getOne(knjz020nQuery::getNameSpare2($this, "L004"));
            $this->applicantdiv = $db->getOne(knjz020nQuery::getNameMstSet($this, "L003", $appNamespare));
            $this->testdiv = $db->getOne(knjz020nQuery::getNameMstSet($this, "L004", $testNamespare));
            Query::dbCheckIn($db);
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
        if (strlen($this->field["TESTDIV"]) == 0){
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["TOTALCD"]) == 0){
            $this->setWarning("MSG301","( 受験コース )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["TESTSUBCLASSCD"]) == 0){
            $this->setWarning("MSG301","( 試験科目 )");
            return false;
        }
        //空文字チェック
        if (($this->cmd == "add" || $this->cmd == "update") && strlen($this->field["PERFECT"]) == 0){
            $this->setWarning("MSG301","( 満点 )");
            return false;
        }
        //キー値が変更されているかをチェック
        if (($this->field["TESTSUBCLASSCD"] != $this->testsubclasscd ) && 
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $query = knjz020nQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->totalcd, $this->field["TESTSUBCLASSCD"]);
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
        if (strlen($this->field["PERFECT"]) > 3){
            $this->setWarning("MSG915","( 満点は45バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjz020nQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjz020nQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        $this->totalcd = $this->field["TOTALCD"];
        $this->testsubclasscd = $this->field["TESTSUBCLASSCD"];
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjz020nQuery::getDeleteQuery($this->field);
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
            $query = knjz020nQuery::getCopyQuery($this->year, $this->nextYear);
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
    $query = knjz020nQuery::getCheckPerfectMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjz020nQuery::getCheckPerfectMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
