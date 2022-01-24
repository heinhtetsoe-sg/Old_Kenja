<?php

require_once('for_php7.php');

//定数定義
class knjz020cModel extends Model {
    var $field      = array();
    var $cmd;
    var $totalcd;          //ENTEXAMYEAR + TESTDIV + APPLICANTDIV + COURSECD + MAJORCD + EXAMCOURSECD(ハイフン区切り）
    var $entexamyear;
    var $applicantdiv;
    var $testdiv;
    var $testsubclasscd;
    var $coursecd;
    var $majorcd;
    var $coursecode;
    var $NextYear;

    //メンバ変数の初期化
    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->totalcd);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->coursecode);
            unset($this->testsubclasscd);
        }

        $this->cmd  = VARS::request("cmd");
        if (VARS::request("TOTALCD")) {
            $this->totalcd = VARS::request("TOTALCD");
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        if (VARS::get("year") || VARS::post("year")) {
            $this->totalcd = $this->year . substr($this->totalcd, 4);
        }

        if (VARS::get("TESTSUBCLASSCD")) {
            $this->testsubclasscd = VARS::get("TESTSUBCLASSCD");
        }

        if ($this->totalcd) {
            list($this->field["ENTEXAMYEAR"],
                 $this->field["APPLICANTDIV"],
                 $this->field["TESTDIV"],
                 $this->field["COURSECD"],
                 $this->field["MAJORCD"],
                 $this->field["EXAMCOURSECD"]) = preg_split("/-/", $this->totalcd);
        }


        $this->field["TESTSUBCLASSCD"] = VARS::request("TESTSUBCLASSCD");  //試験科目コード
        $this->field["PERFECT"]        = VARS::post("PERFECT");            //満点
        $this->field["UPDATED"]        = VARS::post("UPDATED");

    }
    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["ENTEXAMYEAR"]) == 0){
            $this->setWarning("MSG301","( 年度 )");
            return false;
        }
        if (strlen($this->field["TESTSUBCLASSCD"]) == 0){
            $this->setWarning("MSG301","( 試験科目 )");
            return false;
        }
        if (strlen($this->field["PERFECT"]) == 0){
            $this->setWarning("MSG301","( 満点 )");
            return false;
        }
        //キー値が変更されているかをチェック
        if (($this->field["TESTSUBCLASSCD"] != $this->testsubclasscd) && ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //ENTEXAM_SCORE_DATのMAX(score)と更新する満点との比較
        if($kind=="update"){
            $maxscore = knjz020cQuery::ChScoreDat($this);
            if($maxscore > $this->field["PERFECT"]){
                $this->setWarning("MSG300","入力されている".$maxscore."点より低い満点は設定出来ません。");
                return false;
            }
        }
        //ENTEXAM_SCORE_DATで使用されてないかチェック
        if($kind=="delete"){
            $scoredat = knjz020cQuery::ChScoreDat($this);
            if($scoredat > 0){
                $this->setWarning("MSG300","得点が設定されています。");
                return false;
            }
        }

        //１レコード取得
        $query = knjz020cQuery::CheckData($this);
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
        //更新日付チェック1
        if ($this->cmd == "update" && $row["UPDATED"] == ""){
            $this->setWarning("MSG303");
            return false;
        }
        //更新日付チェック2
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["PRRFECT"]) > 3){
            $this->setWarning("MSG915","(満点は3バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz020cQuery::getDeleteQuery($this);
            knjz020cQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjz020cQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->testdiv = $this->field["TESTDIV"];
            $this->testsubclasscd = $this->field["TESTSUBCLASSCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz020cQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
            $this->testdiv = "";
            $this->testsubclasscd = "";
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
        if (!checkNextYearCourseMst($db, $this->nextYear)) {
            $message = $message . '次年度の受験コースマスタがありません。\n事前に受験コースマスタを作成してください。';
            $flg = false;
        }
        if ($flg) {                                                             //エラーがなければデータ更新
            $query = knjz020cQuery::getCopyQuery($this->year, $this->nextYear);
            $db->autoCommit(false);
            $db->query($query);
            $db->commit();
            $this->setMessage("MSG201");
        } else {                                                                 //エラーがあればエラーを表示
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
    $query = knjz020cQuery::getCheckPerfectMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjz020cQuery::getCheckPerfectMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}

function checkNextYearCourseMst($db, $nextYear) {
    $query = knjz020cQuery::getCheckNextYearCourseMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}
?>
