<?php

require_once('for_php7.php');

//定数定義
class knjl035fModel extends Model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $item_cd;           //primary
    var $exemption_cd;      //primary
    var $year;              //入試年度
    var $NextYear;

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->item_cd);
            unset($this->exemption_cd);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                        //入試年度
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "ITEM_CD"          =>  VARS::post("ITEM_CD"),              //費目コード
                             "EXEMPTION_CD"     =>  VARS::post("EXEMPTION_CD"),         //諸費コード
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
        $temp = VARS::get("EXEMPTION_CD");
        if (isset($temp)) {
            $this->exemption_cd = VARS::get("EXEMPTION_CD");
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if (strlen($this->field["ITEM_CD"]) == 0) {
            $this->setWarning("MSG301","( 費目コード )");
            return false;
        }
        if (strlen($this->field["EXEMPTION_CD"]) == 0) {
            $this->setWarning("MSG301","( 諸費コード )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["APPLICANTDIV"] != $this->applicantdiv ||
             $this->field["ITEM_CD"] != $this->item_cd ||
             $this->field["EXEMPTION_CD"] != $this->exemption_cd ) &&
            ($kind == "delete")) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjl035fQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["ITEM_CD"], $this->field["EXEMPTION_CD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918","( 諸費コード・費目コード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl035fQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->applicantdiv = $this->field["APPLICANTDIV"];
            $this->item_cd      = $this->field["ITEM_CD"];
            $this->exemption_cd = $this->field["EXEMPTION_CD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl035fQuery::getDeleteQuery($this->field);
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
            $query = knjl035fQuery::getCopyQuery($this->year, $this->nextYear);
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
    $query = knjl035fQuery::getCheckCopySql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjl035fQuery::getCheckCopySql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}
?>
