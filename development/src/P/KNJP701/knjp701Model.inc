<?php

require_once('for_php7.php');

//定数定義
class knjp701Model extends Model {
    var $field = array();
    var $cmd;
    var $collect_l_cd;       //学校払込コード
    var $collect_bank_div;      //銀行種別

    //メンバ変数の初期化
    function knjp701Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->collect_l_cd);
            unset($this->schoolKind);
        }

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        if (VARS::get("COLLECT_L_CD")) {
            $this->collect_l_cd = sprintf("%02d",VARS::get("COLLECT_L_CD"));
        }

        $this->field = array("COLLECT_L_CD"      => VARS::post("COLLECT_L_CD"),         //学校払込コード
                             "COLLECT_L_NAME"    => VARS::post("COLLECT_L_NAME"),       //学校払込名称
                             "COLLECT_L_ABBV"    => VARS::post("COLLECT_L_ABBV"),       //銀行種別
                             "LEVY_FLG"          => VARS::post("LEVY_FLG"),             //預り金対象科目フラグ
                             "UPDATED"           => VARS::post("UPDATED"));
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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

        //COLLECT_M_MSTに登録されているものは削除不可
        if ($kind=="delete") {
            //COLLECT_M_MSTの１レコード取得
            $countM = knjp701Query::getCollectMcd($this, $this->collect_l_cd);
            if ($countM != 0) {
                $this->setWarning("MSG919", "項目マスタにデータが登録されています。");
                return false;
            }
        }

        //入力項目の１レコード取得
        $row = knjp701Query::getRow($this, $this->field["COLLECT_L_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjp701Query::getRow($this, $this->collect_l_cd );
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
            knjp701Query::getInsertQuery($this, $this->field);
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
            knjp701Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjp701Query::getDeleteQuery($this, $this->field);
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
