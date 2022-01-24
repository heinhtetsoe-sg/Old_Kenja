<?php

require_once('for_php7.php');

//定数定義
class knjp747aModel extends Model {
    var $field = array();
    var $cmd;
    var $schoolKind;    //校種
    var $formatDiv;     //フォーマット区分
    var $bankCd;        //銀行コード

    //メンバ変数の初期化
    function knjp747aModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->schoolKind);
            unset($this->formatDiv);
            unset($this->bankCd);
        }

        $this->cmd = VARS::request("cmd");
        $this->schoolKind   = VARS::request("SCHOOL_KIND");
        $this->formatDiv    = VARS::request("FORMAT_DIV");
        $this->bankCd       = VARS::request("BANK_CD");
        $this->seq        = '001'; // 現状は"001"固定（2019.04.04）

        $this->field = array("SCHOOL_KIND"          => VARS::post("SCHOOL_KIND"),       //校種
                             "FORMAT_DIV"           => VARS::post("FORMAT_DIV"),        //フォーマット区分
                             "SHUBETSU_CD"          => VARS::post("SHUBETSU_CD"),       //種別コード<
                             "CODE_DIV"             => VARS::post("CODE_DIV"),          //コード区分
                             "ACCOUNT_CD"           => VARS::post("ACCOUNT_CD"),        //委託者コード
                             "ACCOUNTNAME_KANA"     => VARS::post("ACCOUNTNAME_KANA"),  //委託者名(ｶﾅ)
                             "BANK_CD"              => VARS::post("BANK_CD"),           //銀行コード
                             "BANKNAME_KANA"        => VARS::post("BANKNAME_KANA"),     //銀行名(ｶﾅ)
                             "BRANCHCD"             => VARS::post("BRANCHCD"),          //支店コード
                             "BRANCHNAME_KANA"      => VARS::post("BRANCHNAME_KANA"),   //支店名(ｶﾅ)
                             "DEPOSIT_TYPE"         => VARS::post("DEPOSIT_TYPE"),      //預金種目
                             "ACCOUNTNO"            => VARS::post("ACCOUNTNO"),         //口座番号
                             "BANK_TRANSFER_FEE"    => VARS::post("BANK_TRANSFER_FEE"), //引落手数料
                             "TARGET_BANK_DIV"      => VARS::post("TARGET_BANK_DIV"),   //対象銀行区分
                             "TARGET_BANK_CD"       => VARS::post("TARGET_BANK_CD"),    //対象銀行コード
                             "UPDATED"              => VARS::post("UPDATED"));

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //項目名セット
        $db = Query::dbCheckOut();
        $this->nameArr = array();
        $query = knjp747aQuery::getNameMst('P006');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->nameArr[$row["NAMECD2"]] = $row["NAME1"];
        }
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput($kind) {
        $checkField = array();
        $checkField["SCHOOL_KIND"]          = array('HISSU',    0,     '校種');
        $checkField["FORMAT_DIV"]           = array('HISSU',    0,     'フォーマット区分');
        $checkField["SHUBETSU_CD"]          = array('HISSU',    2,     $this->nameArr[2]);
        $checkField["CODE_DIV"]             = array('HISSU',    1,     $this->nameArr[3]);
        $checkField["ACCOUNT_CD"]           = array('HISSU',    10,    $this->nameArr[4]);
        $checkField["ACCOUNTNAME_KANA"]     = array('HISSU',    120,   $this->nameArr[5]);
        $checkField["BANK_CD"]              = array('HISSU',    4,     $this->nameArr[7]);
        $checkField["BANKNAME_KANA"]        = array('HISSU',    45,    $this->nameArr[8]);
        $checkField["BRANCHCD"]             = array('HISSU',    3,     $this->nameArr[9]);
        $checkField["BRANCHNAME_KANA"]      = array('HISSU',    45,    $this->nameArr[10]);
        $checkField["DEPOSIT_TYPE"]         = array('HISSU',    1,     $this->nameArr[11]);
        $checkField["ACCOUNTNO"]            = array('HISSU',    7,     $this->nameArr[12]);
        $checkField["BANK_TRANSFER_FEE"]    = array('HISSU',    3,     '引落手数料');
        $checkField["TARGET_BANK_DIV"]          = array('HISSU',    1,   '対象銀行区分');
        if ($this->field["TARGET_BANK_DIV"] == '2') {
            $checkField["TARGET_BANK_CD"]       = array('HISSU',    4,     '対象銀行コード');
        }

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
        if (($kind=="update" || $kind=="delete") && (($this->schoolKind != $this->field["SCHOOL_KIND"]) || ($this->formatDiv != $this->field["FORMAT_DIV"]) || ($this->bankCd != $this->field["BANK_CD"]))){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjp747aQuery::getRow($this, $this->field["SCHOOL_KIND"], $this->field["FORMAT_DIV"], $this->field["BANK_CD"]);
        //重複チェック
        if ($kind == "insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        $row2 = knjp747aQuery::getRow($this, $this->schoolKind, $this->formatDiv, $this->bankCd);
        if (($kind == "update" || $kind == "delete") && !is_array($row2)) {
            $this->setWarning("MSG303");
            return false;
        }
        //対象銀行区分の不一致チェック
        $targetDivArray = knjp747aQuery::getTargetBankDiv($this, $this->field["SCHOOL_KIND"], $this->field["FORMAT_DIV"], $this->field["BANK_CD"]);
        $checkTargetDiv = $this->field["TARGET_BANK_DIV"] == "1" ? "2" : "1";
        $messageTargetDiv = $this->field["TARGET_BANK_DIV"] == "1" ? "特定銀行" : "全て";
        if (($kind == "insert" || $kind == "update") && $targetDivArray[$checkTargetDiv] == "1") {
            $this->setWarning("対象銀行区分:【{$messageTargetDiv}】の登録データが存在します。\\n{$messageTargetDiv}以外の登録は出来ません。");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if($this->validateInput("insert")){
            knjp747aQuery::getInsertQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if($this->validateInput("update")){
            knjp747aQuery::getUpdateQuery($this, $this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if($this->validateInput("delete")){
            knjp747aQuery::getDeleteQuery($this, $this->field);
            $this->schoolKind = $this->formatDiv = $this->bankCd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp747aQuery::CheckData($this, CTRL_YEAR);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp747aQuery::CheckData($this, (CTRL_YEAR - 1));
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp747aQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean() {
    }
}
?>
