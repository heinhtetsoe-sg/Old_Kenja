<?php

require_once('for_php7.php');

//定数定義
class knjz021cModel extends Model {
    var $field      = array();
    var $cmd;
    var $entexamyear;
    var $applicantdiv;
    var $preexamtype; //追加
    var $testsubclasscd; //残し
    var $perfect;
    var $NextYear;

    //メンバ変数の初期化
    function init()  {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        
        $this->field = array("ENTEXAMYEAR"    => VARS::post("ENTEXAMYEAR"),
                             "APPLICANTDIV"   => VARS::post("APPLICANTDIV"),
                             "PRE_EXAM_TYPE"  => VARS::post("PRE_EXAM_TYPE"),
                             "TESTSUBCLASSCD" => VARS::post("TESTSUBCLASSCD"),
                             "PERFECT"        => VARS::post("PERFECT"),
                             "UPDATED"        => VARS::post("UPDATED")
                             );
        
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->preexamtype);
            unset($this->testsubclasscd);
            unset($this->perfect);
        }
        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp1 = VARS::request("ENTEXAMYEAR");
        if (isset($temp1)) {
            $this->entexamyear  = VARS::request("ENTEXAMYEAR");
            $this->field["ENTEXAMYEAR"] = $this->entexamyear;
        }
        $temp2 = VARS::request("APPLICANTDIV");
        if (isset($temp2)) {
            $this->applicantdiv  = VARS::request("APPLICANTDIV");
            $this->field["APPLICANTDIV"] = $this->applicantdiv;
        }
        $temp3 = VARS::get("PRE_EXAM_TYPE");
        if (isset($temp3)) {
            $this->preexamtype  = VARS::get("PRE_EXAM_TYPE");
            $this->field["PRE_EXAM_TYPE"] = $this->preexamtype;
        }
        $temp4 = VARS::get("TESTSUBCLASSCD");
        if (isset($temp4)) {
            $this->testsubclasscd  = VARS::get("TESTSUBCLASSCD");
            $this->field["TESTSUBCLASSCD"] = $this->testsubclasscd;
        }
        $temp5 = VARS::get("PERFECT");
        if (isset($temp5)) {
            $this->perfect  = VARS::get("PERFECT");
            $this->field["PERFECT"] = $this->perfect;
        }
        
    }
    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["PRE_EXAM_TYPE"]) == 0){    
            $this->setWarning("MSG301","( 受験型 )");
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
        if (($this->field["PRE_EXAM_TYPE"] != $this->preexamtype) && ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->field["TESTSUBCLASSCD"] != $this->testsubclasscd) && ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //ENTEXAM_SCORE_PRE_DATのMAX(score)と更新する満点との比較
        if ($this->cmd == "update") {
            $maxscore = knjz021cQuery::ChScoreDat($this);
            if($maxscore > $this->field["PERFECT"]){
                $this->setWarning("MSG300","入力されている".$maxscore."点より低い満点は設定出来ません。");
                return false;
            }
        }
        //ENTEXAM_SCORE_PRE_DATで使用されてないかチェック
        if ($this->cmd == "delete") {
            $scoredat = knjz021cQuery::ChScoreDat($this);
            if($scoredat > 0){
                $this->setWarning("MSG300","得点が設定されています。");
                return false;
            }
        }
        //重複データチェック
        $query = knjz021cQuery::CheckData($this, $this->entexamyear, $this->applicantdiv);
        $db = Query::dbCheckOut();
        $Check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        //重複チェック
        if ($this->cmd == "add") {
            if ($Check == 1) {
                $this->setWarning("MSG918");
                return false;
            }
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
            knjz021cQuery::getDeleteQuery($this, $this->entexamyear, $this->applicantdiv);
            knjz021cQuery::getInsertQuery($this, $this->entexamyear, $this->applicantdiv);
            $this->setMessage("MSG201");
        }
        return;
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
                knjz021cQuery::getInsertQuery($this, $this->entexamyear, $this->applicantdiv);
            $this->setMessage("MSG201");
            $this->preexamtype = $this->field["PRE_EXAM_TYPE"];
            $this->testsubclasscd = $this->field["TESTSUBCLASSCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz021cQuery::getDeleteQuery($this, $this->entexamyear, $this->applicantdiv);
            $this->setMessage("MSG201");
            $this->preexamtype = "";
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
        if ($flg) {                                                             //エラーがなければデータ更新
            $query = knjz021cQuery::getCopyQuery($this->year, $this->nextYear);
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
    $query = knjz021cQuery::getCheckPerfectMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjz021cQuery::getCheckPerfectMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
