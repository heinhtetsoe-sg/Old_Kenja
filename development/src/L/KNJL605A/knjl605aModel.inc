<?php

require_once('for_php7.php');

//定数定義
class knjl605aModel extends Model {
    var $field      = array();
    var $cmd;

    var $year;
    var $NextYear;

    var $examYear;
    var $applicantDiv;
    var $testDiv;
    var $scholarCd;

    var $examcourseCd;

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examYear);
            unset($this->applicantDiv);
            unset($this->testDiv);
            unset($this->scholarCd);
        }

        if (VARS::request("YEAR")) {
            $this->year = VARS::request("YEAR");
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("ENTEXAMYEAR"      =>  VARS::post("ENTEXAMYEAR")     //入試年度
                            , "APPLICANTDIV"    =>  VARS::post("APPLICANTDIV")    //入試制度
                            , "TESTDIV"         =>  VARS::post("TESTDIV")         //入試区分

                            , "SP_SCHOLAR_CD"   =>  VARS::post("SP_SCHOLAR_CD")   //特別奨学生コード
                            , "SP_SCHOLAR_NAME" =>  VARS::post("SP_SCHOLAR_NAME") //特別奨学生名称
                        );

        // 対象コース取得
        $this->field['EXAMCOURSE'] = array();
        if (VARS::post("SELECT_COURSE")) {
            $this->field['EXAMCOURSE'] = explode(',', VARS::post("SELECT_COURSE"));
        }

        if (VARS::get("ENTEXAMYEAR")){
            $this->examYear = VARS::get("ENTEXAMYEAR");
        }
        if (VARS::get("APPLICANTDIV")){
            $this->applicantDiv = VARS::get("APPLICANTDIV");
        }
        if (VARS::get("TESTDIV")){
            $this->testDiv = VARS::get("TESTDIV");
        }
        if (VARS::get("SP_SCHOLAR_CD")){
            $this->scholarCd = VARS::get("SP_SCHOLAR_CD");
        }

    }

    //入力項目チェック
    function validateInput($kind)
    {

        if ($this->cmd != "delete") {
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
            if (strlen($this->field["SP_SCHOLAR_CD"]) == 0){
                $this->setWarning("MSG301","( 特別奨学生コード )");
                return false;
            }
            //空文字チェック
            if (strlen($this->field["SP_SCHOLAR_NAME"]) == 0){
                $this->setWarning("MSG301","( 特別奨学生名称 )");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["SP_SCHOLAR_NAME"]) > 60){
                $this->setWarning("MSG915","( 特別奨学生は60バイトまでです。)");
                return false;
            }

            //空文字チェック
            if (get_count($this->field["EXAMCOURSE"]) < 1){
            $this->setWarning("MSG301","( 対象コース )");
            return false;
            }
        }

        //キー値が変更されているかをチェック
        if (($this->field["ENTEXAMYEAR"]   != $this->examYear ||
             $this->field["APPLICANTDIV"]  != $this->applicantDiv ||
             $this->field["TESTDIV"]       != $this->testDiv ||
             $this->field["SP_SCHOLAR_CD"] != $this->scholarCd) && ($kind == "update" || $kind == "delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl605aQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->field["SP_SCHOLAR_CD"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918","( 特別奨学生コード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            // 特別奨学生マスタ登録
            $query = knjl605aQuery::getInsertSpScholarshipMstQuery($this->field);
            $db->query($query);

            // 特別奨学生コースデータは 削除後に登録
            $query = knjl605aQuery::getDeleteSpScholarshipDatQuery($this->field);
            $db->query($query);
            for ($i=0; $i < get_count($this->field['EXAMCOURSE']); $i++) {
                $course = $this->field['EXAMCOURSE'][$i];
                $query = knjl605aQuery::getInsertSpScholarshipDatQuery($this->field, $course);
                $db->query($query);
            }

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
        }
        return;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            // 特別奨学生マスタ更新
            $query = knjl605aQuery::getUpdateSpScholarshipMstQuery($this->field);
            $db->query($query);

            // 特別奨学生コースデータは 削除後に登録
            $query = knjl605aQuery::getDeleteSpScholarshipDatQuery($this->field);
            $db->query($query);
            for ($i=0; $i < get_count($this->field['EXAMCOURSE']); $i++) {
                $course = $this->field['EXAMCOURSE'][$i];
                $query = knjl605aQuery::getInsertSpScholarshipDatQuery($this->field, $course);
                $db->query($query);
            }

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            // 特別奨学生マスタ更新
            $query = knjl605aQuery::getDeleteSpScholarshipMstQuery($this->field);
            $db->query($query);

            // 特別奨学生コースデータは 削除後
            $query = knjl605aQuery::getDeleteSpScholarshipDatQuery($this->field);
            $db->query($query);

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
            $this->field = array();
        }
        return;
    }

    //次年度のデータを作成
    function &getCopyYearModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this->nextYear = $this->year + 1;
        $message = '';
        $flg = true;
        if (!checkThisYear($db, $this->year)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (checkThisYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if (!checkCourse($db, $this->year, $this->nextYear)) {
            $message = $message . '次年度に対象のコースが設定されていません。。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl605aQuery::getCopyQuery($this->year, $this->nextYear);
            $db->query($query);

            $query = knjl605aQuery::getCopySubQuery($this->year, $this->nextYear);
            $db->query($query);

            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //変数をクリアします
    function clean() {
    }
}

/**
 * TRUE  : メインテーブルにデータが存在
 * FALSE : メインテーブルにデータが存在しない
 */
function checkThisYear($db, $year) {
    $query = knjl605aQuery::getCheckSpScholarshipMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}
/**
 * TRUE  ： 対象コースが登録されている
 * FALSE ： 対象コースが登録されていない
 */
function checkCourse($db, $year, $nextYear) {
    $query = knjl605aQuery::getCheckCourseSql($year, $nextYear);
    $result = $db->getOne($query);
    if ($result > 0) {
        return false;
    }
    return true;
}
?>
