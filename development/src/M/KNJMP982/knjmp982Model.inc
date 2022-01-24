<?php

require_once('for_php7.php');

//定数定義
class knjmp982Model extends Model {
    var $field = array();
    var $cmd;
    var $levy_lmcd;      //会計科目+項目コード
    var $levy_scd;       //費目小分類コード
    var $year;

    //メンバ変数の初期化
    function knjmp982Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->auth);
            unset($this->levy_lmcd);
            unset($this->levy_scd);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        if (VARS::get("LEVY_L_M_CD")) {
            $this->levy_lmcd = VARS::get("LEVY_L_M_CD");
        }
        if (VARS::get("LEVY_S_CD")) {
            $this->levy_scd = sprintf("%02d",VARS::get("LEVY_S_CD"));
        }

        $this->field = array("YEAR"           => $this->year,
                             "LEVY_L_M_CD"   => VARS::post("LEVY_L_M_CD"),   //会計科目・項目コード
                             "LEVY_S_CD"     => VARS::post("LEVY_S_CD"),     //会計細目コード
                             "LEVY_S_NAME"   => VARS::post("LEVY_S_NAME"),   //会計細目名称
                             "LEVY_S_ABBV"   => VARS::post("LEVY_S_ABBV"),   //会計細目略称
                             "REPAY_DIV"     => VARS::post("REPAY_DIV"),     //返金可・不可
                             "REMARK"        => VARS::post("REMARK")         //備考
                             );
    }

    //入力項目チェック
    function validateInput($kind)
    {
        if(strlen($this->field["LEVY_L_M_CD"]) == 0){
            $this->setWarning("MSG301","(会計科目・項目コード)");
            return false;
        }
        if(strlen($this->field["LEVY_S_CD"]) == 0){
            $this->setWarning("MSG301","(会計細目コード)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["LEVY_S_NAME"]) == 0){
                $this->setWarning("MSG301","(会計細目名称)");
                return false;
            }
        }

        if(($kind=="update" || $kind=="delete") && $this->levy_scd != $this->field["LEVY_S_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjmp982Query::getRow($this,2);
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
        if (strlen($this->field["LEVY_S_NAME"]) > 90){
            $this->setWarning("MSG915","(会計細目名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["LEVY_S_ABBV"]) > 90){
            $this->setWarning("MSG915","(会計細目略称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 60){
            $this->setWarning("MSG915","(備考は60バイトまでです。)");
            return false;
        }
        
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjmp982Query::getInsertQuery($this->field);
            $this->levy_lmcd = $this->field["LEVY_L_M_CD"];
            $this->levy_scd = sprintf("%02d",$this->field["LEVY_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjmp982Query::getUpdateQuery($this->field);
            $this->levy_lmcd = $this->field["LEVY_L_M_CD"];
            $this->levy_scd = sprintf("%02d",$this->field["LEVY_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjmp982Query::getDeleteQuery($this->field);
            $this->levy_lmcd = "";
            $this->levy_scd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //今年度のデータが存在するか確認
        $t_year = knjmp982Query::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjmp982Query::CheckData($this->year - 1);
        if(!isset($pre_year)){
          $this->setWarning('MSG203', "前年データが存在しません。");
          return false;
        }

        knjmp982Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
