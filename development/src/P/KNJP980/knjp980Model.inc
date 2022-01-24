<?php

require_once('for_php7.php');

//定数定義
class knjp980Model extends Model {
    var $field = array();
    var $cmd;
    var $levy_l_cd;       //学校払込コード
    var $levy_bank_div;      //銀行種別

    //メンバ変数の初期化
    function knjp980Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->levy_l_cd);
            unset($this->schoolKind);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        if (VARS::get("LEVY_L_CD")) {
            $this->levy_l_cd = sprintf("%02d",VARS::get("LEVY_L_CD"));
        }

        $this->field = array("LEVY_L_CD"            => VARS::post("LEVY_L_CD"),             //学校払込コード
                             "LEVY_L_NAME"          => VARS::post("LEVY_L_NAME"),           //学校払込名称
                             "LEVY_L_ABBV"          => VARS::post("LEVY_L_ABBV"),           //銀行種別
                             "UPDATED"              => VARS::post("UPDATED"));
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["LEVY_L_CD"]) == 0){
            $this->setWarning("MSG301","(コード)");
            return false;
        }
        
        if ($kind == "insert" || $kind == "update") {
            if(strlen($this->field["LEVY_L_NAME"]) == 0){
                $this->setWarning("MSG301","(名称)");
                return false;
            }
        }
        //バイト数チェック
        if (strlen($this->field["LEVY_L_NAME"]) > 90){
            $this->setWarning("MSG915","(名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["LEVY_L_ABBV"]) > 90){
            $this->setWarning("MSG915","(略称は90バイトまでです。)");
            return false;
        }

        //キー変更チェック
        if (($kind=="update" || $kind=="delete") && $this->levy_l_cd != $this->field["LEVY_L_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjp980Query::getRow($this, $this->field["LEVY_L_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjp980Query::getRow($this, $this->levy_l_cd);
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
            knjp980Query::getInsertQuery($this);
            $this->levy_l_cd = sprintf("%02d",$this->field["LEVY_L_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjp980Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjp980Query::getDeleteQuery($this);
            $this->levy_l_cd = "";
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
