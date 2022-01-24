<?php

require_once('for_php7.php');

class knjz517Model extends Model {
    var $field = array();
    var $cmd;
    var $service_centercd;
    var $areacd;

    function knjz517Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->service_centercd);
            unset($this->service_centercd_edaban);
            unset($this->areacd);
        }

        $this->field = array("SERVICE_CENTERCD" =>  VARS::post("SERVICE_CENTERCD"),
                             "SERVICE_CENTERCD_EDABAN"  => VARS::post("SERVICE_CENTERCD_EDABAN"),
                             "NAME"             =>  VARS::post("NAME"),
                             "ABBV"             =>  VARS::post("ABBV"),
                             "AREACD"           =>  VARS::post("AREACD"),
                             "ZIPCD"            =>  VARS::post("ZIPCD"),
                             "ADDR1"            =>  VARS::post("ADDR1"),
                             "ADDR2"            =>  VARS::post("ADDR2"),
                             "ADDR3"            =>  VARS::post("ADDR3"),
                             "COMMISSION_NAME"  =>  VARS::post("COMMISSION_NAME"),
                             "TELNO"            =>  VARS::post("TELNO"),
                             "FAXNO"            =>  VARS::post("FAXNO"),
                             "CHALLENGED_SUPPORT_FLG"   =>  VARS::post("CHALLENGED_SUPPORT_FLG")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SERVICE_CENTERCD")) {
            $this->service_centercd = VARS::get("SERVICE_CENTERCD");
        }
        if (VARS::get("SERVICE_CENTERCD_EDABAN")) {
            $this->service_centercd_edaban = VARS::get("SERVICE_CENTERCD_EDABAN");
        }

        if (VARS::request("LEFT_AREACD")) {
            $this->areacd = VARS::request("LEFT_AREACD");
        }
        if (VARS::post("LEFT_AREACD")) {
            $this->areacd = VARS::post("LEFT_AREACD");
            unset($this->service_centercd);
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if (trim($this->field["SERVICE_CENTERCD"]) == "") {
            $this->setWarning("MSG301", "　　　　　　(　コード　)");
            return false;
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (trim($this->field["NAME"]) == "") {
                $this->setWarning("MSG301", "　　　　　(　事業所名　)");
                return false;
            }
            if (trim($this->field["AREACD"]) == "") {
                $this->setWarning("MSG301", "　　　　　(　圏域　)");
                return false;
            }
        }

        //頭ゼロ埋め
        if (strlen($this->field["SERVICE_CENTERCD"]) <= 10 ) {
            $this->field["SERVICE_CENTERCD"] = sprintf("%010s", $this->field["SERVICE_CENTERCD"]);
        }
        if (strlen($this->field["SERVICE_CENTERCD_EDABAN"]) <= 2 ) {
            $this->field["SERVICE_CENTERCD_EDABAN"] = sprintf("%02s", $this->field["SERVICE_CENTERCD_EDABAN"]);
        }

        //キーの更新チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && ($this->service_centercd != $this->field["SERVICE_CENTERCD"] || $this->service_centercd_edaban != $this->field["SERVICE_CENTERCD_EDABAN"])) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && !strlen($this->service_centercd) && !strlen($this->service_centercd_edaban)) {
            $this->setWarning("MSG308");
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz517Query::getWelfare_useServiceCenterMst($this->field["SERVICE_CENTERCD"], $this->field["SERVICE_CENTERCD_EDABAN"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //更新対象がなければエラー
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //追加時 既にレコードがあればエラー
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (strlen($this->field["SERVICE_CENTERCD"]) > 10 ) {
                $this->setWarning("MSG915", "(コード) 10バイトまでです。");
                return false;
            }
            if (strlen($this->field["SERVICE_CENTERCD_EDABAN"]) > 2 ) {
                $this->setWarning("MSG915", "(コードの枝番) 2バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["NAME"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(事業所名) 全角50文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ABBV"], (20 * 2)) > 1) {
                $this->setWarning("MSG915", "(略称名) 全角20文字までです。");
                return false;
            }
            if (strlen($this->field["AREACD"]) > 2) {
                $this->setWarning("MSG915", "(圏域) 2バイトまでです。");
                return false;
            }
            if (strlen($this->field["ZIPCD"]) > 8 ) {
                $this->setWarning("MSG915", "(郵便番号) 8バイトまでです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ADDR1"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(住所１) 全角50文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ADDR2"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(住所２) 全角50文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["ADDR3"], (50 * 2)) > 1) {
                $this->setWarning("MSG915", "(住所３) 全角50文字までです。");
                return false;
            }
            if ($this->validate_row_cnt($this->field["COMMISSION_NAME"], (30 * 2)) > 1) {
                $this->setWarning("MSG915", "(法人(設置名)) 全角30文字までです。");
                return false;
            }
            if (strlen($this->field["TELNO"]) > 14 ) {
                $this->setWarning("MSG915", "(電話番号) 14バイトまでです。");
                return false;
            }
            if (strlen($this->field["FAXNO"]) > 14 ) {
                $this->setWarning("MSG915", "(ＦＡＸ) 14バイトまでです。");
                return false;
            }
            if (strlen($this->field["CHALLENGED_SUPPORT_FLG"]) > 1 ) {
                $this->setWarning("MSG915", "(障害者支援施設) 1バイトまでです。");
                return false;
            }
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        if ($this->validateInput()){
            knjz517Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz517Query::getInsertQuery($this);
            $this->service_centercd = $this->field["SERVICE_CENTERCD"];
            $this->service_centercd_edaban = $this->field["SERVICE_CENTERCD_EDABAN"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz517Query::getDeleteQuery($this);
            unset($this->service_centercd);
            unset($this->service_centercd_edaban);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }
}
?>
