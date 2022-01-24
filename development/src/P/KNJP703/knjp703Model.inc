<?php

require_once('for_php7.php');

//定数定義
class knjp703Model extends Model {
    var $field = array();
    var $cmd;
    var $exp_lmcd;      //会計科目+項目コード
    var $exp_scd;       //費目小分類コード
    var $year;
    var $calledId;      //呼び出し元

    //メンバ変数の初期化
    function knjp703Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->auth);
            unset($this->exp_lmcd);
            unset($this->exp_scd);
            unset($this->calledId);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        if (VARS::get("COLLECT_L_M_CD")) {
            $this->exp_lmcd = VARS::get("COLLECT_L_M_CD");
        }
        if (VARS::get("COLLECT_S_CD")) {
            $this->exp_scd = sprintf("%02d",VARS::get("COLLECT_S_CD"));
        }

        $this->field = array("YEAR"             => $this->year,
                             "COLLECT_L_M_CD"   => VARS::post("COLLECT_L_M_CD"),   //会計科目・項目コード
                             "COLLECT_S_CD"     => VARS::post("COLLECT_S_CD"),     //会計細目コード
                             "COLLECT_S_NAME"   => VARS::post("COLLECT_S_NAME"),   //会計細目名称
                             "COLLECT_S_MONEY"  => VARS::post("COLLECT_S_MONEY")   //会計細目金額
                             //"SEX"              => VARS::post("SEX")               //性別
                             );

        if (VARS::get("mode") != ""){
            $this->calledId = VARS::get("mode");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        if(strlen($this->field["COLLECT_L_M_CD"]) == 0){
            $this->setWarning("MSG301","(会計科目・項目コード)");
            return false;
        }
        if(strlen($this->field["COLLECT_S_CD"]) == 0){
            $this->setWarning("MSG301","(会計細目コード)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["COLLECT_S_NAME"]) == 0){
                $this->setWarning("MSG301","(会計細目名称)");
                return false;
            }
            if(strlen($this->field["COLLECT_S_MONEY"]) == 0){
                $this->setWarning("MSG301","(金額)");
                return false;
            }
        }

        if(($kind=="update" || $kind=="delete") && $this->exp_scd != $this->field["COLLECT_S_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjp703Query::getRow($this,2);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["COLLECT_S_NAME"]) > 60){
            $this->setWarning("MSG915","(会計細目名称は60バイトまでです。)");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjp703Query::getInsertQuery($this->field);
            $this->exp_lmcd = $this->field["COLLECT_L_M_CD"];
            $this->exp_scd = sprintf("%02d",$this->field["COLLECT_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjp703Query::getUpdateQuery($this->field);
            $this->exp_lmcd = $this->field["COLLECT_L_M_CD"];
            $this->exp_scd = sprintf("%02d",$this->field["COLLECT_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjp703Query::getDeleteQuery($this->field);
            $this->exp_lmcd = "";
            $this->exp_scd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //今年度のデータが存在するか確認
        $t_year = knjp703Query::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp703Query::CheckData($this->year - 1);
        if(!isset($pre_year)){
          $this->setWarning('MSG203', "前年データが存在しません。");
          return false;
        }

        knjp703Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
