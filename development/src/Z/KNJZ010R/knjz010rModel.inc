<?php

require_once('for_php7.php');

//定数定義
class knjz010rModel extends Model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $testdiv;           //primary
    var $coursecd;          //primary
    var $majorcd;           //primary
    var $examcoursecd;      //primary
    var $year;              //入試年度
    var $NextYear;
    var $fixApplicantDiv;

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->examcoursecd);
            unset($this->totalcd);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                            //入試年度
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),             //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),                  //入試区分
                             "TOTALCD"          =>  VARS::post("TOTALCD"),                  //課程・学科コード
                             "COURSECD"         =>  substr(VARS::post("TOTALCD"), 0, 1),    //課程コード
                             "MAJORCD"          =>  substr(VARS::post("TOTALCD"), 1, 3),    //学科コード
                             "EXAMCOURSECD"     =>  VARS::post("EXAMCOURSECD"),             //コースコード
                             "EXAMCOURSE_NAME"  =>  VARS::post("EXAMCOURSE_NAME"),          //コース名
                             "EXAMCOURSE_ABBV"  =>  VARS::post("EXAMCOURSE_ABBV"),          //コース名略称
                             "EXAMCOURSE_MARK"  =>  VARS::post("EXAMCOURSE_MARK"),          //コース記号
                             "CAPACITY"         =>  VARS::post("CAPACITY"),                 //コース記号
                             "ENTER_TOTALCD"    =>  VARS::post("ENTER_TOTALCD"),                //入学課程・学科コード
                             "ENTER_COURSECD"   =>  substr(VARS::post("ENTER_TOTALCD"), 0, 1),  //入学課程コード
                             "ENTER_MAJORCD"    =>  substr(VARS::post("ENTER_TOTALCD"), 1, 3),  //入学学科コード
                             "ENTER_COURSECODE" =>  VARS::post("ENTER_COURSECODE"),             //入学コースコード
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("APPLICANTDIV");
        if (isset($temp)){
            $this->applicantdiv = VARS::get("APPLICANTDIV");
        }
        $temp = VARS::get("TESTDIV");
        if (isset($temp)){
            $this->testdiv = VARS::get("TESTDIV");
        }
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd = VARS::get("COURSECD");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd = VARS::get("MAJORCD");
        }
        $temp = VARS::get("EXAMCOURSECD");
        if (isset($temp)){
            $this->examcoursecd = VARS::get("EXAMCOURSECD");
        }

        if(VARS::get("TOTALCD")){
            $this->totalcd  = VARS::get("TOTALCD");
            $this->coursecd = substr($this->totalcd,0,1);
            $this->majorcd  = substr($this->totalcd,1,3);
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
        if (strlen($this->field["TESTDIV"]) == 0){
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }
        if (strlen($this->field["COURSECD"]) == 0){
            $this->setWarning("MSG301","( 課程コード )");
            return false;
        }
        if (strlen($this->field["MAJORCD"]) == 0){
            $this->setWarning("MSG301","( 学科コード )");
            return false;
        }
        if ((int)$this->field["EXAMCOURSECD"] < 1){
           $this->setWarning("MSG901","( コースコードには１以上を入力して下さい。 )");
           return false;
        }
        if (strlen($this->field["EXAMCOURSECD"]) == 0){
            $this->setWarning("MSG301","( コースコード )");
            return false;
        }
        if (strlen($this->field["EXAMCOURSE_NAME"]) == 0){
            $this->setWarning("MSG301","( コース名 )");
            return false;
        }
        if (strlen($this->field["ENTER_COURSECD"]) == 0){
            $this->setWarning("MSG301","( 入学課程コード )");
            return false;
        }
        if (strlen($this->field["ENTER_MAJORCD"]) == 0){
            $this->setWarning("MSG301","( 入学学科コード )");
            return false;
        }
        if (strlen($this->field["ENTER_COURSECODE"]) == 0){
            $this->setWarning("MSG301","( 入学コースコード )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["APPLICANTDIV"] != $this->applicantdiv ||
             $this->field["TESTDIV"]      != $this->testdiv ||
             $this->field["COURSECD"]     != $this->coursecd ||
             $this->field["MAJORCD"]      != $this->majorcd  ||
             $this->field["EXAMCOURSECD"] != $this->examcoursecd ) && 
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //コードのゼロ埋め
        $this->field["EXAMCOURSECD"] = sprintf("%04d",$this->field["EXAMCOURSECD"]);
        //１レコード取得
        $query = knjz010rQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["EXAMCOURSECD"], $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->field["COURSECD"], $this->field["MAJORCD"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918","( コースコード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["EXAMCOURSE_NAME"]) > 45){
            $this->setWarning("MSG915","( コース名は45バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["EXAMCOURSE_ABBV"]) > 30){
            $this->setWarning("MSG915","( コース名略称は30バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjz010rQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjz010rQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->examcoursecd = $this->field["EXAMCOURSECD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjz010rQuery::getDeleteQuery($this->field);
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
        if (!checkThisYear($db, $this, $this->year)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!checkNextYear($db, $this, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjz010rQuery::getCopyQuery($this, $this->year, $this->nextYear);
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

function checkThisYear($db, $this, $year) {
    $query = knjz010rQuery::getCheckCourseMstSql($this, $year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $this, $nextYear) {
    $query = knjz010rQuery::getCheckCourseMstSql($this, $nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
