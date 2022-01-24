<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp900_schregModel extends Model {
    var $field      = array();
    var $cmd;
    var $getAuth;
    var $getPrgId;
    var $getSchoolKind;
    var $getYear;
    var $getIncomeLcd;
    var $getIncomeMcd;
    var $getIncomeLMcd;
    var $getRequestNo;
    var $getCollectLMScd;
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
    function knjp900_schregModel()
    {
    }

    function init()
    {
        if (VARS::get("SEND_AUTH")) {
            $this->getAuth         = VARS::get("SEND_AUTH");
            $this->getSchoolKind   = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear         = VARS::get("SEND_YEAR");
            $this->getPrgId        = VARS::get("SEND_PRGID");
            $this->getIncomeLcd    = VARS::get("SEND_INCOME_L_CD");
            $this->getIncomeMcd    = VARS::get("SEND_INCOME_M_CD");
            $this->getIncomeLMcd   = VARS::get("SEND_INCOME_L_M_CD");
            $this->getRequestNo    = VARS::get("SEND_REQUEST_NO");
            $this->getCollectLMScd = VARS::get("SEND_COLLECT_L_M_S_CD");
            $this->getLineNo       = VARS::get("SEND_LINE_NO");
        }

        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        $this->field = array("WARIHURI_DIV"     =>  VARS::post("WARIHURI_DIV"),      //1:生徒、2:生徒以外切替
                             "COMMODITY_NAME"   =>  VARS::post("COMMODITY_NAME"),    //会費名等
                             "COMMODITY_PRICE"  =>  VARS::post("COMMODITY_PRICE"),   //単価
                             "COMMODITY_CNT"    =>  VARS::post("COMMODITY_CNT"),     //数量
                             "TOTAL_PRICE"      =>  VARS::post("TOTAL_PRICE"),       //消費税
                             "REMARK"           =>  VARS::post("REMARK"),            //備考
                             "HR_CLASS_HYOUJI_FLG" =>  VARS::post("HR_CLASS_HYOUJI_FLG"),  //年組番表示
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );
        //年組表示を初期値とする
        if (VARS::get("SEND_AUTH")) {
            $this->field["HR_CLASS_HYOUJI_FLG"] = "1";
        }
        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");
        $this->movesFlg = VARS::post("movesFlg");

        $this->cmd         = VARS::request("cmd");
    }
    
    //入力項目チェック
    function validateInput($db)
    {
        //金額チェック
        $errorFlg = false;
        $setSchregNo = "";
        $conma = "";
        $count = 0;
        $query = knjp900_schregQuery::getSchnoMoneyData($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["ZAN_GK"] < $this->field["COMMODITY_PRICE"]) {
                $setSchregNo .= $conma.$row["SCHREGNO"];
                $conma = ",";
                $errorFlg = true;
            }
            $count++;
        }
        $result->free();
        if ($errorFlg) {
            $this->setWarning("MSG203", "単価が生徒の入金残高を超えています。(".$setSchregNo.")");
            return false;
        }
        //バイトチェック
        if (strlen($this->field["COMMODITY_NAME"]) > 90) {
            $this->setWarning("MSG901", "( 会費名等 ) 90バイトまで");
            return false;
        } else if (strlen($this->field["COMMODITY_PRICE"]) > 6) {
            $this->setWarning("MSG901", "( 単価 ) 6バイトまで");
            return false;
        } else if (strlen($this->field["REMARK"]) > 30) {
            $this->setWarning("MSG901", "( 備考 ) 30バイトまで");
            return false;
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            knjp900_schregQuery::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjp900_schregQuery::getDeleteQuery($db, $this);
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

