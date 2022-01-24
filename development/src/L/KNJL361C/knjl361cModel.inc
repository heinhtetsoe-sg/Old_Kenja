<?php

require_once('for_php7.php');

//定数定義
class knjl361cModel extends Model {
    var $field      = array();
    var $cmd;
    var $itemcd;      //primary
    var $money_boy;
    var $money_girl;
    var $year;        //入試年度
    var $NextYear;

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->itemcd);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                     //入試年度
                             "ITEMCD"           =>  VARS::post("ITEMCD"),            //商品コード
                             "ITEMNAME"         =>  VARS::post("ITEMNAME"),          //商品名
                             "MONEY_BOY"        =>  VARS::post("MONEY_BOY"),         //男子金額
                             "MONEY_GIRL"       =>  VARS::post("MONEY_GIRL"),        //女子金額
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");


        $temp = VARS::get("ITEMCD");
        if (isset($temp)){
            $this->itemcd     = VARS::get("ITEMCD");
            $this->itemname   = VARS::get("ITEMNAME");
            $this->money_boy  = VARS::get("MONEY_BOY");
            $this->money_girl = VARS::get("MONEY_GIRL");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["ITEMCD"]) == 0){
            $this->setWarning("MSG301","( 商品コード )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["ITEMNAME"]) == 0){
            $this->setWarning("MSG301","( 商品名 )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["ITEMCD"] != $this->itemcd) && ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }
        
        //１レコード取得
        $query = knjl361cQuery::getRow($this->field["ENTEXAMYEAR"],$this->field["ITEMCD"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918","( 商品コード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] == ""){
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["ITEMNAME"]) > 30){
            $this->setWarning("MSG915","( 商品名は30バイトまでです。)");
            return false;
        }
        
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjl361cQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjl361cQuery::getInsertQuery($this->field);
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
            knjl361cQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->examcoursecd = "";
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
            $query = knjl361cQuery::getCopyQuery($this->year, $this->nextYear);
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
    $query = knjl361cQuery::getCheckCommodityMstSql($year);
    $result = $db->getOne($query);
    if ($result > 0) {
        return true;
    }
    return false;
}

function checkNextYear($db, $nextYear) {
    $query = knjl361cQuery::getCheckCommodityMstSql($nextYear);
    $result = $db->getOne($query);
    if ($result == 0) {
        return true;
    }
    return false;
}

?>
