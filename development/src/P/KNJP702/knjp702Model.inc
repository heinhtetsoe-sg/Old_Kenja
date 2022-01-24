<?php

require_once('for_php7.php');
//定数定義
class knjp702Model extends Model {
    var $field = array();
    var $cmd;
    var $exp_lcd;       //入金科目コード
    var $exp_mcd;       //入金項目コード
    var $year;
    var $exist_flg;

    //メンバ変数の初期化
    function knjp702Model()
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
            unset($this->schoolKind);
        }

        $this->year = CTRL_YEAR;
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::request("SCHOOL_KIND");
        $this->monthArray = array("4", "5", "6", "7", "8", "9", "10", "11", "12", "1", "2", "3");

        if (VARS::get("COLLECT_L_CD")) {
            $this->exp_lcd = VARS::get("COLLECT_L_CD");
        }

        if (VARS::get("COLLECT_M_CD")) {
            $this->exp_mcd = sprintf("%02d",VARS::get("COLLECT_M_CD"));
        }
        $this->exp_scd = "00";//固定

        $this->field = array("YEAR"                 => $this->year,
                             "COLLECT_L_CD"         => VARS::post("COLLECT_L_CD"),          //入金科目コード
                             "COLLECT_M_CD"         => VARS::post("COLLECT_M_CD"),          //入金項目コード
                             "COLLECT_M_NAME"       => VARS::post("COLLECT_M_NAME"),        //入金項目名称
                             "COLLECT_M_MONEY"      => VARS::post("COLLECT_M_MONEY"),       //年額
                             "SGL_ITEM"             => VARS::post("SGL_ITEM"),              //SIGEL項目
                             "KOUHI_SHIHI"          => VARS::post("KOUHI_SHIHI"),           //学納金/預り金ラジオ
                             "GAKUNOKIN_DIV"        => VARS::post("GAKUNOKIN_DIV"),         //学納金区分
                             "REDUCTION_DIV"        => VARS::post("REDUCTION_DIV"),         //支援・補助区分
                             "IS_REDUCTION_SCHOOL"  => VARS::post("IS_REDUCTION_SCHOOL"),   //学校減免フラグ
                             "IS_CREDITCNT"         => VARS::post("IS_CREDITCNT"),          //単位数での算出フラグ
                             "IS_REPAY"             => VARS::post("IS_REPAY"),              //返金対象フラグ
                             "TEXTBOOKDIV"          => VARS::post("TEXTBOOKDIV"),           //教科書区分コード
                             "SHOW_ORDER"           => VARS::post("SHOW_ORDER"),            //表示順
                             "REMARK"               => VARS::post("REMARK"),                //備考
                             "DIVIDE_PROCESS"       => VARS::post("DIVIDE_PROCESS"),        //分割端数
                             "ROUND_DIGIT"          => VARS::post("ROUND_DIGIT")            //入金額のまるめ
                             );
        $this->monthCnt = 0;
        foreach ($this->monthArray as $key => $val) {
            $setName = "COLLECT_MONTH_{$val}";
            $this->field[$setName] = VARS::post($setName);
            if (VARS::post($setName)) $this->monthCnt++;
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        $this->exist_flg = "";
        //空文字チェック
        if(strlen($this->field["COLLECT_L_CD"]) == 0){
            $this->setWarning("MSG301","(入金項目コード)");
            return false;
        }
        if(strlen($this->field["COLLECT_M_CD"]) == 0){
            $this->setWarning("MSG301","(入金項目コード)");
            return false;
        }
        if($kind == "insert" || $kind == "update") {
            if(strlen($this->field["COLLECT_M_NAME"]) == 0) {
                $this->setWarning("MSG301","(入金項目名称)");
                return false;
            }
            if(strlen($this->field["COLLECT_M_MONEY"]) == 0) {
                $this->setWarning("MSG301","(年額)");
                return false;
            }
            if ($this->monthCnt == 0) {
                $this->setWarning("MSG301","(入金計画)");
                return false;
            }
        }
        //文字数チェック
        if (strlen($this->field["COLLECT_M_NAME"]) > 90){
            $this->setWarning("MSG915","(入金項目名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["COLLECT_M_MONEY"]) > 8) {
            $this->setWarning("MSG915","( 年額は8バイトまでです。)");
            return false;
        }
        if (strlen($this->field["SHOW_ORDER"]) > 2) {
            $this->setWarning("MSG915","( 表示順は2バイトまでです。)");
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

        //COLLECT_GRP_DATに登録されているものは削除不可
        if ($kind=="delete") {
            //COLLECT_GRP_DATのカウント取得
            $countG = knjp702Query::getCollectGrpDat($this, $this->exp_lcd, $this->exp_mcd, $this->exp_scd);
            if ($countG != 0) {
                $this->setWarning("MSG919", "入金グループにデータが登録されています。");
                return false;
            }
        }

        //入力項目の１レコード取得
        $row = knjp702Query::getRow($this,2);
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

        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if($this->validateInput("insert")){
            knjp702Query::getInsertQuery($this, $this->field);
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
            knjp702Query::getUpdateQuery($this, $this->field);
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
            knjp702Query::getDeleteQuery($this, $this->field);
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
        $t_year = knjp702Query::CheckData($this, $this->year);
        if(isset($t_year)){
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp702Query::CheckData($this, $this->year - 1);
        if(!isset($pre_year)){
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }
        //前年度の大分類コードが今年度に設定されているかチェック
        $lcode = knjp702Query::CheckLcode($this, $this->year -1);
        if(!isset($lcode)){
           $this->setWarning('MSG203', "前年度の入金科目コードが今年度に存在しません。");
           return false;
        }

        knjp702Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
