<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp920_meisaiModel extends Model {
    var $field      = array();
    var $cmd;
    var $getAuth;
    var $getPrgId;
    var $getYear;
    var $getKounyuLcd;
    var $getKounyuMcd;
    var $getKounyuLMcd;
    var $getRequestNo;
    var $getLineNo;
    var $auth;
    var $year;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knjmp920_meisaiModel()
    {
    }

    function init()
    {
        if (VARS::get("SEND_AUTH")) {
            $this->getAuth         = VARS::get("SEND_AUTH");
            $this->getYear         = VARS::get("SEND_YEAR");
            $this->getPrgId        = VARS::get("SEND_PRGID");
            $this->getKounyuLcd    = VARS::get("SEND_KOUNYU_L_CD");
            $this->getKounyuMcd    = VARS::get("SEND_KOUNYU_M_CD");
            $this->getKounyuLMcd   = VARS::get("SEND_KOUNYU_L_M_CD");
            $this->getRequestNo    = VARS::get("SEND_REQUEST_NO");
            $this->getLineNo       = VARS::get("SEND_LINE_NO");
        }

        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        $this->field = array("KOUNYU_L_M_S_CD"      =>  VARS::post("KOUNYU_L_M_S_CD"),      //購入細目
                             "COMMODITY_PRICE"      =>  VARS::post("COMMODITY_PRICE"),      //単価
                             "COMMODITY_CNT"        =>  VARS::post("COMMODITY_CNT"),        //数量
                             "TOTAL_TAX"            =>  VARS::post("TOTAL_TAX"),            //消費税
                             "TOTAL_PRICE_ZEINUKI"  =>  VARS::post("TOTAL_PRICE_ZEINUKI"),  //合計金額(税抜き)
                             "TOTAL_PRICE"          =>  VARS::post("TOTAL_PRICE"),          //合計金額(税込)
                             "REMARK"               =>  VARS::post("REMARK"),               //摘要
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }
    
    //入力項目チェック
    function validateInput($db)
    {
        //必須チェック
        if ($this->field["KOUNYU_L_M_S_CD"] == "") {
            $this->setWarning("MSG301", "( 品名等 )");
            return false;
        }
        //重複チェック
        $outgoscd = substr($this->field["KOUNYU_L_M_S_CD"], 4, 2);
        $getCountData = $db->getOne(knjmp920_meisaiQuery::getMeisaiData($this, $outgoscd, "ETC_LINE_NO"));
        if ($getCountData > 0) {
                $this->setWarning("MSG302", "指定した品名は既に他の行で登録済みです。");
            return false;
        }
        //桁数チェック
        if (strlen($this->field["COMMODITY_PRICE"]) > 6) {
            $this->setWarning("MSG901", "( 単価 ) 6バイトまで");
            return false;
        } else if (strlen($this->field["REMARK"]) > 120) {
            $this->setWarning("MSG901", "( 摘要 ) 120バイトまで");
            return false;
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            knjmp920_meisaiQuery::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjmp920_meisaiQuery::getDeleteQuery($db, $this);
        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

        return;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>

