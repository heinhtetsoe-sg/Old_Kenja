<?php

require_once('for_php7.php');

//定数定義
class knjp747Model extends Model {
    var $field = array();
    var $cmd;
    var $schoolKind;     //校種
    var $formatDiv;      //フォーマット区分

    //メンバ変数の初期化
    function knjp747Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->schoolKind);
            unset($this->formatDiv);
        }

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::request("SCHOOL_KIND");
        $this->formatDiv  = VARS::request("FORMAT_DIV");
        $this->seq        = '001'; // 現状は"001"固定（2019.04.04）

        $this->field = array("SCHOOL_KIND"          => VARS::post("SCHOOL_KIND"),       //校種
                             "FORMAT_DIV"           => VARS::post("FORMAT_DIV"),        //フォーマット区分
                             "SHUBETSU_CD"          => VARS::post("SHUBETSU_CD"),       //契約種別コード
                             "JC_CD"                => VARS::post("JC_CD"),             //JCコード
                             "ACCOUNT_CD"           => VARS::post("ACCOUNT_CD"),        //事業主番号
                             "ACCOUNTNAME_KANA"     => VARS::post("ACCOUNTNAME_KANA"),  //事業主名(ｶﾅ)
                             "BANK_CD"              => VARS::post("BANK_CD"),           //金融機関コード
                             "BANKNAME_KANA"        => VARS::post("BANKNAME_KANA"),     //金融機関名(ｶﾅ)
                             "BRANCHCD"             => VARS::post("BRANCHCD"),          //振替口座記号
                             "ACCOUNTNO"            => VARS::post("ACCOUNTNO"),         //振替口座番号
                             "BANK_TRANSFER_FEE"    => VARS::post("BANK_TRANSFER_FEE"), //手数料
                             "UPDATED"              => VARS::post("UPDATED"));

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        $checkField = array();
        $checkField["SCHOOL_KIND"]          = array('HISSU',    2,     '校種');
        $checkField["FORMAT_DIV"]           = array('HISSU',    0,     'フォーマット区分');
        $checkField["SHUBETSU_CD"]          = array('HISSU',    2,     '契約種別コード');
        if ($this->field["FORMAT_DIV"] == '1') {
            $feeTitle = '引落手数料';
            $checkField["BRANCHCD"]             = array('HISSU',    3,     '振替口座記号');
        } else {
            $feeTitle = '返金手数料';
            $checkField["JC_CD"]                = array('',    2,     'JCコード');
        }
        $checkField["ACCOUNT_CD"]           = array('HISSU',    8,     '事業主番号');
        $checkField["ACCOUNTNAME_KANA"]     = array('HISSU',    120,   '事業主名');
        $checkField["BANK_CD"]              = array('HISSU',    4,     '金融機関コード');
        $checkField["BANKNAME_KANA"]        = array('HISSU',    45,    '金融機関名');
        $checkField["ACCOUNTNO"]            = array('HISSU',    6,     '振替口座番号');
        $checkField["BANK_TRANSFER_FEE"]    = array('HISSU',    3,     $feeTitle);

        if ($kind=="update" || $kind=="insert") {
            foreach ($checkField as $fieldName => $valArr) {
                //空文字チェック
                if ($valArr[0] == 'HISSU') {
                    if(strlen($this->field[$fieldName]) == 0){
                        $this->setWarning("MSG301","({$valArr[2]})");
                        return false;
                    }
                }

                //バイト数チェック
                if ($valArr[1] > 0) {
                    if (strlen($this->field[$fieldName]) > $valArr[1]){
                        $this->setWarning("MSG915","({$valArr[2]}は{$valArr[1]}バイトまでです。)");
                        return false;
                    }
                }
            }
        }

        //キーチェック
        if (($kind=="update" || $kind=="delete") && (($this->schoolKind != $this->field["SCHOOL_KIND"]) || ($this->formatDiv != $this->field["FORMAT_DIV"]))){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjp747Query::getRow($this, $this->field["SCHOOL_KIND"], $this->field["FORMAT_DIV"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjp747Query::getRow($this, $this->schoolKind, $this->formatDiv);
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row2)) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if($this->validateInput("insert")){
            knjp747Query::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if($this->validateInput("update")){
            knjp747Query::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if($this->validateInput("delete")){
            knjp747Query::getDeleteQuery($this, $this->field);
            $this->schoolKind = $this->formatDiv = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp747Query::CheckData($this, CTRL_YEAR);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp747Query::CheckData($this, (CTRL_YEAR - 1));
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp747Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
