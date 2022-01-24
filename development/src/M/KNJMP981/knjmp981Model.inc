<?php

require_once('for_php7.php');

//定数定義
class knjmp981Model extends Model {
    var $field = array();
    var $cmd;
    var $levy_lcd;       //会計科目コード
    var $levy_mcd;       //会計項目コード
    var $s_exist_flg;   //会計細目有無フラグ
    var $year;
    var $exist_flg;
    var $levy_in_out_div;

    //メンバ変数の初期化
    function knjmp981Model()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->auth);
            unset($this->levy_lcd);
            unset($this->levy_mcd);
            unset($this->exist_flg);
            unset($this->levy_in_out_div);
        }

        $this->year = CTRL_YEAR;
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd = VARS::request("cmd");
        $this->s_exist_flg =  VARS::post("LEVY_S_EXIST_FLG");
        //初期値（初期値）
        $this->levy_in_out_div = ($this->levy_in_out_div == "") ? "all" : $this->levy_in_out_div;

        if (VARS::get("LEVY_L_CD")) {
            $this->levy_lcd = VARS::get("LEVY_L_CD");
        }

        if (VARS::get("LEVY_M_CD")) {
            $this->levy_mcd = sprintf("%02d",VARS::get("LEVY_M_CD"));
        }

        $this->field = array("YEAR"             => $this->year,
                             "LEVY_L_CD"        => VARS::post("LEVY_L_CD"),          //会計科目コード
                             "LEVY_M_CD"        => VARS::post("LEVY_M_CD"),          //会計項目コード
                             "LEVY_IN_OUT_DIV"  => VARS::post("LEVY_IN_OUT_DIV"),    //収入支出区分
                             "LEVY_M_NAME"      => VARS::post("LEVY_M_NAME"),        //会計項目名称
                             "LEVY_M_ABBV"      => VARS::post("LEVY_M_ABBV"),        //会計項目略称
                             "LEVY_S_EXIST_FLG" => VARS::post("LEVY_S_EXIST_FLG"),   //会計細目有無
                             "ZATU_FLG"         => VARS::post("ZATU_FLG"),           //雑収入の設定
                             "YOBI_FLG"         => VARS::post("YOBI_FLG"),           //予備費の設定
                             "KURIKOSI_FLG"     => VARS::post("KURIKOSI_FLG"),       //繰越金の設定
                             "REMARK"           => VARS::post("REMARK")              //備考
                             );

        if (VARS::post("SET_LEVY_IN_OUT_DIV") != "") {
            $this->levy_in_out_div = VARS::post("SET_LEVY_IN_OUT_DIV");  //会計科目コード
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        $this->exist_flg = "";
        //空文字チェック
        if(strlen($this->field["LEVY_L_CD"]) == 0){
            $this->setWarning("MSG301","(会計項目コード)");
            return false;
        }
        if(strlen($this->field["LEVY_M_CD"]) == 0){
            $this->setWarning("MSG301","(会計項目コード)");
            return false;
        }
        if(strlen($this->field["LEVY_IN_OUT_DIV"]) == 0){
            $this->setWarning("MSG301","(収入支出区分)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["LEVY_M_NAME"]) == 0) {
                $this->setWarning("MSG301","(会計項目名称)");
                return false;
            }
            if(strlen($this->field["LEVY_S_EXIST_FLG"]) == 0) {
                $this->setWarning("MSG301","(会計細目有無)");
                return false;
            }
        }
        //文字数チェック
        if (strlen($this->field["LEVY_M_NAME"]) > 90){
            $this->setWarning("MSG915","(会計項目名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["LEVY_M_ABBV"]) > 90){
            $this->setWarning("MSG915","(会計項目略称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 60){
            $this->setWarning("MSG915","(備考は60バイトまでです。)");
            return false;
        }
        
        if(($kind=="update" || $kind=="delete") && ($this->levy_mcd !== $this->field["LEVY_M_CD"] || $this->levy_lcd !== $this->field["LEVY_L_CD"])){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjmp981Query::getRow($this,2);
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
        $countdata = $db->getOne(knjmp981Query::countLevySMst($this));
        if (($kind == "update") && ($this->field["LEVY_S_EXIST_FLG"] === '2') && ($countdata > 0)) {
            $this->setWarning("MSG203", "会計細目マスタメンテに登録されているデータが存在します。");
            return false;
        } else if (($kind == "delete") && ($countdata > 0)) {
            $this->setWarning("MSG203", "会計細目マスタメンテに登録されているデータが存在します。");
            return false;
        }
        //繰越金チェック
        if ($this->field["KURIKOSI_FLG"] === '1') {
            $getLevyLCnt = $db->getOne(knjmp981Query::getLevyKurikosiCnt($this->field["LEVY_L_CD"], $this->field["LEVY_M_CD"]));
            if (($kind == "update" || $kind == "insert") && $getLevyLCnt > 0) {
                $this->setWarning("MSG203", "同一の会計科目コードに繰越金フラグが設定されているデータが存在します。");
                return false;
            }
        }
        Query::dbCheckIn($db);

        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjmp981Query::getInsertQuery($this, $this->field);
            $this->levy_lcd = $this->field["LEVY_L_CD"];
            $this->levy_mcd = sprintf("%02d",$this->field["LEVY_M_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjmp981Query::getUpdateQuery($this, $this->field);
            $this->levy_lcd = $this->field["LEVY_L_CD"];
            $this->levy_mcd = sprintf("%02d",$this->field["LEVY_M_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjmp981Query::getDeleteQuery($this->field);
            $this->levy_lcd = "";
            $this->levy_mcd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        //今年度のデータが存在するか確認
        $t_year = knjmp981Query::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjmp981Query::CheckData($this->year - 1);
        if(!isset($pre_year)){
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }
        //前年度の大分類コードが今年度に設定されているかチェック
        $lcode = knjmp981Query::CheckLcode($this->year);
        if(!isset($lcode)){
           $this->setWarning('MSG203', "前年度の会計科目コードが今年度に存在しません。");
           return false;
        }

        knjmp981Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
