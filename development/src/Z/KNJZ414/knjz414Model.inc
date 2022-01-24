<?php

require_once('for_php7.php');

class knjz414Model extends Model {
    var $field      = array();
    var $cmd;
    var $industry_lcd;        //primary
    var $industry_mcd;        //primary

    function knjz414Model() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->industry_lcd);
            unset($this->industry_mcd);
        }

        $this->field = array("INDUSTRY_LCD"     =>  VARS::post("INDUSTRY_LCD"),
                             "INDUSTRY_LNAME"   =>  VARS::post("INDUSTRY_LNAME"),
                             "INDUSTRY_MCD"     =>  VARS::post("INDUSTRY_MCD"),
                             "INDUSTRY_MNAME"   =>  VARS::post("INDUSTRY_MNAME"),
                             "NO_OUTPUT"        =>  VARS::post("NO_OUTPUT")
                             );

        $this->cmd       = VARS::request("cmd");

        $lcd = VARS::get("INDUSTRY_LCD");
        if (isset($lcd[0])) {
            $this->industry_lcd  = VARS::get("INDUSTRY_LCD");
        }
        $mcd = VARS::get("INDUSTRY_MCD");
        if (isset($mcd)) {
            $this->industry_mcd  = VARS::get("INDUSTRY_MCD");
        }
    }

    //入力項目チェック
    function validateInput() {

        //キーのチェック
        if (trim($this->field["INDUSTRY_LCD"]) == "") {
            $this->setWarning('MSG301',"(大分類コード)");
            return false;
        }
        if (trim($this->field["INDUSTRY_MCD"]) == "") {
            $this->setWarning('MSG301',"(中分類コード)");
            return false;
        }

        if($this->cmd = "update"){
            //キーのチェック
            if (trim($this->field["INDUSTRY_LNAME"]) == "") {
                $this->setWarning('MSG301',"(大分類名称)");
                return false;
            }
            if (trim($this->field["INDUSTRY_MNAME"]) == "") {
                $this->setWarning('MSG301',"(中分類名称)");
                return false;
            }

            //コードのゼロ埋め
            if (strlen($this->field["INDUSTRY_MCD"]) < 2) {
                $this->field["INDUSTRY_MCD"] = sprintf("%02d", $this->field["INDUSTRY_MCD"]);
            }

            if (strlen($this->field["INDUSTRY_LCD"]) >1 ) {
                $this->setWarning('MSG914', "(大分類コード) 1バイトまで");
                return false;
            } elseif (strlen($this->field["INDUSTRY_LNAME"])>90) {
                $this->setWarning('MSG914', "(大分類名称) 90バイトまで");
                return false;
            } elseif (strlen($this->field["INDUSTRY_MCD"])>2) {
                $this->setWarning('MSG914', "(中分類コード) 2バイトまで");
                return false;
            } elseif (strlen($this->field["INDUSTRY_MNAME"])>90) {
                $this->setWarning('MSG914', "(中分類名称) 90バイトまで");
                return false;
            }
        } else {
            //１レコード取得
            $db = Query::dbCheckOut();
            $query = knjz414Query::getIndustryMst($this->field["INDUSTRY_LCD"], $this->field["INDUSTRY_MCD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            if (!is_array($row)) {
                $this->setWarning('MSG303');
                return;
            }
        }
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
            $this->industry_lcd = $this->field["INDUSTRY_LCD"];
            $this->industry_mcd = $this->field["INDUSTRY_MCD"];
        }
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {

        if ($this->validateInput()) {
            knjz414Query::getDeleteQuery($this->field);
            $this->setMessage('MSG201');
        }
    }

    //UPDATE
    function &update() {
        knjz414Query::getUpdateLQuery($this->field);
        knjz414Query::getUpdateMQuery($this->field);
    }

    function clean() {}
}
?>
