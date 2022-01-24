<?php

require_once('for_php7.php');

//定数定義
class knjl233yModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $applicantdiv;      //primary
    var $judge_kind;        //primary

    function knjl233yModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->judge_kind);
        }

        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "JUDGE_KIND"           =>  VARS::post("JUDGE_KIND"),           //合格種別
                             "JUDGE_KIND_NAME"      =>  VARS::post("JUDGE_KIND_NAME"),      //合格種別名
                             "ENT_MONEY"            =>  VARS::post("ENT_MONEY"),            //入学金
                             "ENT_MONEY_NAME"       =>  VARS::post("ENT_MONEY_NAME"),       //入学金名称
                             "FAC_MONEY"            =>  VARS::post("FAC_MONEY"),            //施設費
                             "FAC_MONEY_NAME"       =>  VARS::post("FAC_MONEY_NAME"),       //施設費名称
                             "LESSON_MONEY"         =>  VARS::post("LESSON_MONEY"),         //授業料
                             "LESSON_MONEY_NAME"    =>  VARS::post("LESSON_MONEY_NAME"),    //授業料名称
                             "FAC_MNT_MONEY"        =>  VARS::post("FAC_MNT_MONEY"),        //施設維持費
                             "FAC_MNT_MONEY_NAME"   =>  VARS::post("FAC_MNT_MONEY_NAME"),   //施設維持費名称
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
            $this->setWarning("MSG301","( 合格種別 )");
            return false;
        }
        //空文字チェック
        if ($this->field["APPLICANTDIV"] != "1" && strlen($this->field["JUDGE_KIND_NAME"]) == 0 &&
           ($kind=="add" || $kind=="update")){
            $this->setWarning("MSG301","( 合格種別名 )");
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
        $query = knjl233yQuery::getRow($this->field["APPLICANTDIV"], $this->field["JUDGE_KIND"]);
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
        if (strlen($this->field["JUDGE_KIND_NAME"]) > 60){
            $this->setWarning("MSG915","( 合格種別名は60バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["ENT_MONEY_NAME"]) > 60){
            $this->setWarning("MSG915","( 入学金名称は60バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["FAC_MONEY_NAME"]) > 60){
            $this->setWarning("MSG915","( 施設費名称は60バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["LESSON_MONEY_NAME"]) > 60){
            $this->setWarning("MSG915","( 授業料名称は60バイトまでです。)");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["FAC_MNT_MONEY_NAME"]) > 60){
            $this->setWarning("MSG915","( 施設維持費名称は60バイトまでです。)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjl233yQuery::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjl233yQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjl233yQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }  

    //変数をクリアします
    function clean()
    {
    }
}
?>
