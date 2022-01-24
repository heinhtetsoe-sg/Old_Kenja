<?php

require_once('for_php7.php');

//定数定義
class knjz060kModel extends Model {
    var $field = array();
    var $cmd;
    var $exp_lcd;       //費目大分類コード
    var $exp_mcd;       //費目中分類コード
    var $s_exist_flg;   //費目小分類有無フラグ
    var $year;
    var $exist_flg;

    //メンバ変数の初期化
    function knjz060kModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->exp_lcd);
            unset($this->exp_mcd);
            unset($this->exist_flg);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");
        $this->s_exist_flg =  VARS::post("EXPENSE_S_EXIST_FLG");

        if (VARS::get("EXPENSE_L_CD")) {
            $this->exp_lcd = VARS::get("EXPENSE_L_CD");
        }

        if (VARS::get("EXPENSE_M_CD")) {
            $this->exp_mcd = sprintf("%02d",VARS::get("EXPENSE_M_CD"));
        }

        $this->field = array("YEAR"                 => $this->year,
                             "EXPENSE_L_CD"         => VARS::post("EXPENSE_L_CD"),          //費目大分類コード
                             "EXPENSE_M_CD"         => VARS::post("EXPENSE_M_CD"),          //費目中分類コード
                             "EXPENSE_M_NAME"       => VARS::post("EXPENSE_M_NAME"),        //費目中分類名称
                             "EXPENSE_S_EXIST_FLG"  => VARS::post("EXPENSE_S_EXIST_FLG"),   //費用小分類有無
                             "EXPENSE_M_MONEY"      => VARS::post("EXPENSE_M_MONEY"),       //費目中分類金額
                             "DUE_DATE"             => VARS::post("DUE_DATE"),              //納入期限
                             "BANK_TRANS_SDATE"     => VARS::post("BANK_TRANS_SDATE")       //自動振替日
                             );
    }

    //入力項目チェック
    function validateInput($kind)
    {
        $this->exist_flg = "";
        //空文字チェック
        if(strlen($this->field["EXPENSE_L_CD"]) == 0){
            $this->setWarning("MSG301","(費目大分類コード)");
            return false;
        }
        if(strlen($this->field["EXPENSE_M_CD"]) == 0){
            $this->setWarning("MSG301","(費目中分類コード)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["EXPENSE_M_NAME"]) == 0) {
                $this->setWarning("MSG301","(費目中分類名称)");
                return false;
            }
            if(strlen($this->field["EXPENSE_S_EXIST_FLG"]) == 0) {
                $this->setWarning("MSG301","(費目小分類有無)");
                return false;
            }
            if(strlen($this->field["EXPENSE_M_MONEY"]) == 0 && $this->field["EXPENSE_S_EXIST_FLG"] == "2") {
                $this->setWarning("MSG301","(金額)");
                return false;
            }
            if(strlen($this->field["BANK_TRANS_SDATE"]) == 0) {
                $this->setWarning("MSG301","(自動振替日)");
                return false;
            }            
            if(strlen($this->field["DUE_DATE"])) {
                $due_date  = str_replace("/", "", $this->field["DUE_DATE"]);
                $bank_date = str_replace("/", "", $this->field["BANK_TRANS_SDATE"]);
                if ($due_date > $bank_date) {
                    $this->setWarning("MSG901","納入期限が不正です。\\n再入力して下さい。");
                    return false;
                }
            }
        }
        //文字数チェック
        if (strlen($this->field["EXPENSE_M_NAME"]) > 60){
            $this->setWarning("MSG915","(費目中分類名称は60バイトまでです。)");
            return false;
        }
        if(($kind=="update" || $kind=="delete") && $this->exp_mcd != $this->field["EXPENSE_M_CD"]){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjz060kQuery::getRow($this,2);
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
        //重複チェック２
        if ($kind == "insert" || $kind == "update") {
            $row = knjz060kQuery::ExistData($this);
            if (strlen($row)) {
                $this->setWarning("MSG302","自動振替日が重複しています。");
                return false;
            }
        }

        //小分類が存在するかチェック
        $scode = knjz060kQuery::CheckScode($this);
        if ($kind == "delete" && isset($scode)) {
            $this->setWarning("MSG300","費目小分類マスタが存在します。\\n費目小分類マスタを削除した後、処理を行なって下さい。");
            $this->exist_flg = true;
            return false;
        }            
        if ($kind == "update" && $this->field["EXPENSE_S_EXIST_FLG"] == "2" && isset($scode)) {
            $this->setWarning("MSG300","費目小分類マスタが存在します。\\n費目小分類マスタを削除した後、処理を行なって下さい。");
            $this->exist_flg = true;
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjz060kQuery::getInsertQuery($this->field);
            $this->exp_lcd = $this->field["EXPENSE_L_CD"];
            $this->exp_mcd = sprintf("%02d",$this->field["EXPENSE_M_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput("update")){
            knjz060kQuery::getUpdateQuery($this->field);
            $this->exp_lcd = $this->field["EXPENSE_L_CD"];
            $this->exp_mcd = sprintf("%02d",$this->field["EXPENSE_M_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput("delete")){
            knjz060kQuery::getDeleteQuery($this->field);
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
        $t_year = knjz060kQuery::CheckData($this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjz060kQuery::CheckData($this->year - 1);
        if(!isset($pre_year)){
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }
        //前年度の大分類コードが今年度に設定されているかチェック
        $lcode = knjz060kQuery::CheckLcode($this->year);
        if(!isset($lcode)){
           $this->setWarning('MSG203', "前年度の大分類コードが今年度に存在しません。");
           return false;
        }

        knjz060kQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
