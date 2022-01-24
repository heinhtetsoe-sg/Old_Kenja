<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjp905_mainModel extends Model {

    var $cmd;
    var $field  = array();
    var $subField  = array();
    var $getAuth;
    var $koumoku;
    var $getPrgId;
    var $getSchoolKind;
    var $getYear;
    var $getOutgoLcd;
    var $getOutgoMcd;
    var $getOutgoLMcd;
    var $getRequestNo;
    var $getKounyuRequestNo;
    var $getSekouRequestNo;
    var $getSeisanRequestNo;
    var $auth;
    var $setRow = array();

    function init() {
        $this->cmd         = VARS::request("cmd");
        $this->field = array("OUTGO_L_M_CD"       => VARS::post("OUTGO_L_M_CD"),
                             "INCOME_LM_CD"       => VARS::post("INCOME_LM_CD"),
                             "TOTAL_ZAN_GK"       => VARS::post("TOTAL_ZAN_GK"),
                             "REQUEST_NO"         => VARS::post("REQUEST_NO"),
                             "REQUEST_STAFF"      => VARS::post("REQUEST_STAFF"),
                             "REQUEST_REASON"     => VARS::post("REQUEST_REASON"),
                             "REQUEST_DATE"       => VARS::post("REQUEST_DATE"),
                             "LM_BUDGET_TOTAL"    => VARS::post("BUDGET_LM_TOTAL"),
                             "BUDGET_ZAN"         => VARS::post("BUDGET_ZAN"),
                             "REQUEST_GK"         => VARS::post("REQUEST_GK"),
                             "TRADER_CD"          => VARS::post("TRADER_CD"),
                             "TRADER_NAME"        => VARS::post("TRADER_NAME"),
                             "BANKCD"             => VARS::post("BANKCD"),
                             "BRANCHCD"           => VARS::post("BRANCHCD"),
                             "BANK_DEPOSIT_ITEM"  => VARS::post("BANK_DEPOSIT_ITEM"),
                             "BANK_ACCOUNTNO"     => VARS::post("BANK_ACCOUNTNO"),
                             "ACCOUNTNAME"        => VARS::post("ACCOUNTNAME"),
                             "ACCOUNTNAME_KANA"   => VARS::post("ACCOUNTNAME_KANA"),
                             "PAY_DIV"            => VARS::post("PAY_DIV"),
                             "CHANGE_CLASS"       => VARS::post("CHANGE_CLASS")
                              );

        if (VARS::get("SEND_AUTH")) {
            $this->getAuth                 = VARS::get("SEND_AUTH");
            $this->getPrgId                = VARS::get("SEND_PRGID");
            $this->getSchoolKind           = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear                 = VARS::get("SEND_YEAR");
            $this->getOutgoLcd             = VARS::get("SEND_OUTGO_L_CD");
            $this->getOutgoMcd             = VARS::get("SEND_OUTGO_M_CD");
            $this->getOutgoLMcd            = VARS::get("SEND_OUTGO_L_M_CD");
            $this->getRequestNo            = VARS::get("SEND_REQUEST_NO");
            //以下、他画面より取得
            $this->getKounyuRequestNo      = VARS::get("SEND_KOUNYU_REQUEST_NO");
            $this->getSekouRequestNo       = VARS::get("SEND_SEKOU_REQUEST_NO");
            $this->getSeisanRequestNo      = VARS::get("SEND_SEISAN_REQUEST_NO");
        }
        //摘要項目数
        $this->koumoku = "10";

        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        //サブミット表示用
        for ($i = 1; $i <= $this->koumoku; $i++) {
            $this->setRow["LEVY_S_NAME".$i]     = VARS::request("LEVY_S_NAME".$i);
            $this->setRow["COMMODITY_PRICE".$i] = VARS::request("COMMODITY_PRICE".$i);
            $this->setRow["COMMODITY_CNT".$i]   = VARS::request("COMMODITY_CNT".$i);
            $this->setRow["TOTAL_TAX".$i]       = VARS::request("TOTAL_TAX".$i);
            $this->setRow["TOTAL_PRICE".$i]     = VARS::request("TOTAL_PRICE".$i);
            if ($this->Properties["LevyBudget"] == "1") {
                $this->setRow["BUDGET_MONEY".$i]     = VARS::request("BUDGET_MONEY".$i);
            }
            $this->setRow["REMARK".$i]          = VARS::request("REMARK".$i);

            //行コピー用
            $this->field["COPY_CHECK".$i] = VARS::post("COPY_CHECK".$i);
        }

        //取消
        if ($this->cmd == "cancel") {
            $this->field = array();
        }

        //手数料／備考入力
        if ($this->cmd == "tesuryo_bikou") {
            $this->subSchoolCd      = VARS::get("SUB1_SCHOOLCD");
            $this->subSchoolKind    = VARS::get("SUB1_SCHOOL_KIND");
            $this->subYear          = VARS::get("SUB1_YEAR");
            $this->subOutgoLcd      = VARS::get("SUB1_OUTGO_L_CD");
            $this->subOutgoMcd      = VARS::get("SUB1_OUTGO_M_CD");
            $this->subRequestNo     = VARS::get("SUB1_REQUEST_NO");
        }
        $this->subField = array("REQUEST_TESUURYOU" => VARS::post("REQUEST_TESUURYOU"),
                                "TESUURYOU_SUMMARY" => VARS::post("TESUURYOU_SUMMARY"),
                                "REMARK"            => VARS::post("REMARK")
                                );

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //桁数チェック
        if (strlen($this->field["REQUEST_REASON"]) > 120) {
            $this->setWarning("MSG901", "( 決済理由 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["TRADER_NAME"]) > 120) {
            $this->setWarning("MSG901", "( 業者名 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["BANK_ACCOUNTNO"]) > 7) {
            $this->setWarning("MSG901", "( 口座番号 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["ACCOUNTNAME"]) > 120) {
            $this->setWarning("MSG901", "( 口座名義 ) 7バイトまで");
            return false;
        } else if (strlen($this->field["ACCOUNTNAME_KANA"]) > 120) {
            $this->setWarning("MSG901", "( 口座名義カナ ) 120バイトまで");
            return false;
        }
        
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput()) {
            //新規の場合は伝票番号を発行
            if (!$this->getRequestNo) {
                $getMaxRequestNo = $db->getOne(knjp905_mainQuery::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->getYear.'B00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->getYear.'B'.$setNo;
                }
            }
            if ($this->cmd === 'delete_update') {
                knjp905_mainQuery::getDeleteQuery($db, $this);
            }
            knjp905_mainQuery::getUpdateQuery($db, $this);

            //KEYの更新
            $outgolcd = substr($this->field["OUTGO_L_M_CD"], 0, 2);
            $outgomcd = substr($this->field["OUTGO_L_M_CD"], 2, 2);

            $this->getOutgoLcd            = $outgolcd;
            $this->getOutgoMcd            = $outgomcd;
            $this->getOutgoLMcd           = $this->field["OUTGO_L_M_CD"];

            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();

        knjp905_mainQuery::getDeleteQuery($db, $this, "ALL");

        //購入伺、施行伺、精算票がない場合のみ初期化(パラメータで渡せないため)
        if ($this->getKounyuRequestNo == "" && $this->getSekouRequestNo == "" && $this->getSeisanRequestNo == "") {
            $this->getOutgoLcd  = "";
            $this->getOutgoMcd  = "";
            $this->getOutgoLMcd = "";
        }
        $this->getRequestNo  = "";

        $this->setMessage("MSG201");

        Query::dbCheckIn($db);

        return;
    }

    //コピーのモデルを返します
    function &getCopyModel() {

        knjp905_mainQuery::getCopyQuery($this);

        $this->setMessage("MSG201");

        return;
    }

    /****************/
    /* 手数料／備考 */
    /****************/
    //入力項目チェック
    function validateInputRemark() {
        //桁数チェック
        if (strlen($this->subField["REQUEST_TESUURYOU"]) > 4) {
            $this->setWarning("MSG901", "( 振込手数料 ) 4バイトまで");
            return false;
        } else if (strlen($this->subField["TESUURYOU_SUMMARY"]) > 30) {
            $this->setWarning("MSG901", "( 振込手数料摘要 ) 30バイトまで");
            return false;
        }
        //文字数チェック
        if ($this->validate_row_cnt($this->subField["REMARK"], (30 * 2)) > 2) {
            $this->setWarning("MSG915","(備考は2行までです)");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModelTesuRyoRemark() {
        //入力チェック
        if ($this->validateInputRemark()) {
            knjp905_mainQuery::getUpdateQueryRemark($this);

            $this->setMessage("MSG201");
        }

        return;
    }
}
?>
