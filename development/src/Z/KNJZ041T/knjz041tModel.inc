<?php

require_once('for_php7.php');

//定数定義
class knjz041tModel extends Model {
    var $field      = array();
    var $cmd;
    var $s_insert_update;
    var $l_insert_update;
    var $majorlcd;  //primary
    var $majorscd;   //primary

    //メンバ変数の初期化
    function knjz041tModel() {}

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->majorlcd);
            unset($this->majorscd);
        }

        $this->field = array("MAJORLCD"     =>  VARS::post("MAJORLCD"),    //大学科コード
                             "MAJORLNAME"   =>  VARS::post("MAJORLNAME"),  //大学科名称
                             "MAJORLABBV"   =>  VARS::post("MAJORLABBV"),  //大学科略称
                             "MAJORSCD"     =>  VARS::post("MAJORSCD"),    //小学科コード
                             "MAJORSNAME"   =>  VARS::post("MAJORSNAME"),  //小学科名称
                             "MAJORSABBV"   =>  VARS::post("MAJORSABBV"),  //小学科略称
                             "MAIN_MAJORCD" =>  VARS::post("MAIN_MAJORCD") //(賢者)学科コード
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("MAJORLCD");
        if (isset($temp)){
            $this->majorlcd = VARS::get("MAJORLCD");
        }
        $temp = VARS::get("MAJORSCD");
        if (isset($temp)){
            $this->majorscd = VARS::get("MAJORSCD");
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["MAJORLCD"]) == 0){
            $this->setWarning("MSG301","( 大学科コード )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["MAJORSCD"]) == 0){
            $this->setWarning("MSG301","( 小学科コード )");
            return false;
        }
        //キー値が変更されているかをチェック
        if (($this->field["MAJORLCD"] != $this->majorlcd ||
             $this->field["MAJORSCD"] != $this->majorscd ) &&
            ($kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $db = Query::dbCheckOut();
        $query = knjz041tQuery::getRow_S($this->field["MAJORLCD"],$this->field["MAJORSCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //updateにするかinsertにするか
        if (is_array($row)) {
            $this->s_insert_update = 'update';
        } else {
            $this->s_insert_update = 'insert';
        }
        //１レコード取得
        $query = knjz041tQuery::getRow_L($this->field["MAJORLCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //updateにするかinsertにするか
        if (is_array($row)) {
            $this->l_insert_update = 'update';
        } else {
            $this->l_insert_update = 'insert';
        }

        $query = knjz041tQuery::getRow_S($this->field["MAJORLCD"],$this->field["MAJORSCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //削除の時にレコードがあるかチェック
        if (($this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        Query::dbCheckIn($db);
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")){
            if ($this->l_insert_update == 'update') {
                knjz041tQuery::getUpdateQuery_L($this->field);
            } else {
                knjz041tQuery::getInsertQuery_L($this->field);
            }
            if ($this->s_insert_update == 'update') {
                knjz041tQuery::getUpdateQuery_S($this->field);
            } else {
                knjz041tQuery::getInsertQuery_S($this->field);
            }
            $this->setMessage("MSG201");
            $this->majorlcd = $this->field["MAJORLCD"];
            $this->majorscd = $this->field["MAJORSCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")){
            knjz041tQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            unset($this->majorlcd);
            unset($this->majorscd);
        }
        return;
    }

    //変数をクリアします
    function clean() {}
}
?>
