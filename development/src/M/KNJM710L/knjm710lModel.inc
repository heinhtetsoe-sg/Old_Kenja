<?php

require_once('for_php7.php');

//定数定義
class knjm710lModel extends Model {
    var $field = array();
    var $cmd;
    var $collect_l_cd;       //学校払込コード
    var $collect_bank_div;      //銀行種別

    //メンバ変数の初期化
    function knjm710lModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->collect_l_cd);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("COLLECT_L_CD")) {
            $this->collect_l_cd = sprintf("%02d",VARS::get("COLLECT_L_CD"));
        }

        $this->field = array("COLLECT_L_CD"      => VARS::post("COLLECT_L_CD"),         //学校払込コード
                             "COLLECT_L_NAME"    => VARS::post("COLLECT_L_NAME"),       //学校払込名称
                             "COLLECT_L_ABBV"    => VARS::post("COLLECT_L_ABBV"),       //銀行種別
                             "LEVY_FLG"          => VARS::post("LEVY_FLG"),             //収入対象科目フラグ
                             "UPDATED"           => VARS::post("UPDATED"));
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["COLLECT_L_CD"]) == 0){
            $this->setWarning("MSG301","(コード)");
            return false;
        }
        
        if ($kind == "insert" || $kind == "update") {
            if(strlen($this->field["COLLECT_L_NAME"]) == 0){
                $this->setWarning("MSG301","(名称)");
                return false;
            }
        }            
        //バイト数チェック
        if (strlen($this->field["COLLECT_L_NAME"]) > 90){
            $this->setWarning("MSG915","(名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["COLLECT_L_ABBV"]) > 90){
            $this->setWarning("MSG915","(略称は90バイトまでです。)");
            return false;
        }
        
        if (($kind=="update" || $kind=="delete") && $this->collect_l_cd != $this->field["COLLECT_L_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjm710lQuery::getRow($this->field["COLLECT_L_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjm710lQuery::getRow($this->collect_l_cd );
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row2)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjm710lQuery::getInsertQuery($this->field);
            $this->collect_l_cd = sprintf("%02d",$this->field["COLLECT_L_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjm710lQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjm710lQuery::getDeleteQuery($this->field);
            $this->collect_l_cd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
