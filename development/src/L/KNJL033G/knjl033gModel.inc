<?php

require_once('for_php7.php');

//定数定義
class knjl033gModel extends Model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $item_cd;           //primary
    var $year;              //入試年度
    var $NextYear;

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->item_cd);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                        //入試年度
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "ITEM_CD"          =>  VARS::post("ITEM_CD"),              //費目コード
                             "ITEM_NAME"        =>  VARS::post("ITEM_NAME"),            //費目名
                             "ITEM_ABBV"        =>  VARS::post("ITEM_ABBV"),            //略称名
                             "ITEM_MONEY"       =>  str_replace(",", "", VARS::post("ITEM_MONEY")),           //費目金額
                             "REMARK1"          =>  VARS::post("REMARK1"),              //対象チェックボックス
                             "REMARK3"          =>  VARS::post("REMARK3"),              //対象チェックボックス
                             "REMARK4"          =>  VARS::post("REMARK4"),              //対象チェックボックス
                             "REMARK5"          =>  VARS::post("REMARK5"),              //対象チェックボックス
                             "REMARK7"          =>  VARS::post("REMARK7"),              //対象チェックボックス
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::get("APPLICANTDIV");
        }
        $temp = VARS::get("ITEM_CD");
        if (isset($temp)) {
            $this->item_cd = VARS::get("ITEM_CD");
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if ((int)$this->field["ITEM_CD"] < 1) {
           $this->setWarning("MSG901","( 費目コードには１以上を入力して下さい。 )");
           return false;
        }
        if (strlen($this->field["ITEM_CD"]) == 0) {
            $this->setWarning("MSG301","( 費目コード )");
            return false;
        }
        if (strlen($this->field["ITEM_NAME"]) == 0) {
            $this->setWarning("MSG301","( 費目名 )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["APPLICANTDIV"] != $this->applicantdiv ||
             $this->field["ITEM_CD"] != $this->item_cd ) &&
            ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }
        //コードのゼロ埋め
        $this->field["ITEM_CD"] = sprintf("%02d",$this->field["ITEM_CD"]);
        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjl033gQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["ITEM_CD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918","( 費目コード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["ITEM_NAME"]) > 60) {
            $this->setWarning("MSG915","( 費目名は60バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl033gQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl033gQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->item_cd = $this->field["ITEM_CD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl033gQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    function &getCopyYearModel() {
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
            $query = knjl033gQuery::getCopyQuery($this->year, $this->nextYear);
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
    $query = knjl033gQuery::getCheckCourseMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjl033gQuery::getCheckCourseMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
