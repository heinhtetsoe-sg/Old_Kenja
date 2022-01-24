<?php

require_once('for_php7.php');

//定数定義
class knjm710Model extends Model {
    var $field = array();
    var $cmd;
    var $exp_lcd;       //会計科目コード
    var $exp_mcd;       //会計項目コード
    var $s_exist_flg;   //会計細目有無フラグ
    var $year;
    var $exist_flg;

    //メンバ変数の初期化
    function knjm710Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->auth);
            unset($this->exp_lcd);
            unset($this->exp_mcd);
            unset($this->exist_flg);
        }

        $this->year = CTRL_YEAR;
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd = VARS::request("cmd");
        $this->s_exist_flg =  VARS::post("COLLECT_S_EXIST_FLG");

        if (VARS::get("COLLECT_L_CD")) {
            $this->exp_lcd = VARS::get("COLLECT_L_CD");
        }

        if (VARS::get("COLLECT_M_CD")) {
            $this->exp_mcd = sprintf("%02d",VARS::get("COLLECT_M_CD"));
        }

        $this->field = array("YEAR"                 => $this->year,
                             "COLLECT_L_CD"         => VARS::post("COLLECT_L_CD"),          //会計科目コード
                             "COLLECT_M_CD"         => VARS::post("COLLECT_M_CD"),          //会計項目コード
                             "COLLECT_M_NAME"       => VARS::post("COLLECT_M_NAME"),        //会計項目名称
                             "COLLECT_S_EXIST_FLG"  => VARS::post("COLLECT_S_EXIST_FLG"),   //会計細目有無
                             "COLLECT_M_MONEY"      => VARS::post("COLLECT_M_MONEY"),       //金額
                             "PAY_DIV"              => VARS::post("PAY_DIV"),               //支払区分
                             "PAY_DATE"             => VARS::post("PAY_DATE"),              //納入期限
                             "IS_JUGYOURYOU"        => VARS::post("IS_JUGYOURYOU"),         //授業料フラグ
                             "REMARK"               => VARS::post("REMARK")                 //備考
                             );
    }

    //入力項目チェック
    function validateInput($kind)
    {
        $this->exist_flg = "";
        //空文字チェック
        if(strlen($this->field["COLLECT_L_CD"]) == 0){
            $this->setWarning("MSG301","(会計項目コード)");
            return false;
        }
        if(strlen($this->field["COLLECT_M_CD"]) == 0){
            $this->setWarning("MSG301","(会計項目コード)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["COLLECT_M_NAME"]) == 0) {
                $this->setWarning("MSG301","(会計項目名称)");
                return false;
            }
            if(strlen($this->field["COLLECT_S_EXIST_FLG"]) == 0) {
                $this->setWarning("MSG301","(会計細目有無)");
                return false;
            }
            if(strlen($this->field["COLLECT_M_MONEY"]) == 0 && $this->field["COLLECT_S_EXIST_FLG"] == "2") {
                $this->setWarning("MSG301","(金額)");
                return false;
            }
            if(strlen($this->field["COLLECT_M_MONEY"]) != 0 && $this->field["COLLECT_S_EXIST_FLG"] == "1") {
                $this->setWarning("MSG901","(会計細目有の時は、金額は入力できません。)");
                return false;
            }
            if(strlen($this->field["PAY_DIV"]) == 0) {
                $this->setWarning("MSG301","(支払区分)");
                return false;
            }            
        }
        //文字数チェック
        if (strlen($this->field["COLLECT_M_NAME"]) > 90){
            $this->setWarning("MSG915","(会計項目名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 60){
            $this->setWarning("MSG915","(備考は60バイトまでです。)");
            return false;
        }
        
        if(($kind=="update" || $kind=="delete") && ($this->exp_mcd !== $this->field["COLLECT_M_CD"] || $this->exp_lcd !== $this->field["COLLECT_L_CD"])){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjm710Query::getRow($this,2);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($kind == "update" || $kind == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //会計細目データチェック
        $db = Query::dbCheckOut();
        $countdata = $db->getOne(knjm710Query::countCollectSMst($this));
        Query::dbCheckIn($db);
        if (($kind == "update") && ($this->field["COLLECT_S_EXIST_FLG"] === '2') && ($countdata > 0)) {
            $this->setWarning("MSG203", "会計細目マスタメンテに登録されているデータが存在します。");
            return false;
        } else if (($kind == "delete") && ($countdata > 0)) {
            $this->setWarning("MSG203", "会計細目マスタメンテに登録されているデータが存在します。");
            return false;
        }

        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjm710Query::getInsertQuery($this->field);
            $this->exp_lcd = $this->field["COLLECT_L_CD"];
            $this->exp_mcd = sprintf("%02d",$this->field["COLLECT_M_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjm710Query::getUpdateQuery($this->field);
            $this->exp_lcd = $this->field["COLLECT_L_CD"];
            $this->exp_mcd = sprintf("%02d",$this->field["COLLECT_M_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjm710Query::getDeleteQuery($this->field);
            $this->exp_lcd = "";
            $this->exp_mcd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //今年度のデータが存在するか確認
        $t_year = knjm710Query::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjm710Query::CheckData($this->year - 1);
        if(!isset($pre_year)){
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }
        //前年度の大分類コードが今年度に設定されているかチェック
        $lcode = knjm710Query::CheckLcode($this->year);
        if(!isset($lcode)){
           $this->setWarning('MSG203', "前年度の会計科目コードが今年度に存在しません。");
           return false;
        }

        knjm710Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
