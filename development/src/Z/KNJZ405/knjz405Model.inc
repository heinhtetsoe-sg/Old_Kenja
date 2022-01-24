<?php

require_once('for_php7.php');

class knjz405Model extends Model {
    var $field = array();
    var $cmd;
    var $point_l_cd;
    var $point_m_cd;

    function knjz405Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->point_l_cd);
            unset($this->point_m_cd);
        }

        $this->field = array("POINT_L_CD"   =>  (VARS::post("POINT_L_CD") == "") ? "" : sprintf("%02d", VARS::post("POINT_L_CD")),
                             "REMARK_L"     =>  VARS::post("REMARK_L"),
                             "POINT_M_CD"   =>  (VARS::post("POINT_M_CD") == "") ? "" : sprintf("%02d", VARS::post("POINT_M_CD")),
                             "REMARK_M"     =>  VARS::post("REMARK_M")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("POINT_L_CD") != "") {
            $this->point_l_cd = VARS::get("POINT_L_CD");
        }
        if (VARS::get("POINT_M_CD") != "") {
            $this->point_m_cd = VARS::get("POINT_M_CD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->field["POINT_L_CD"] == "") {
            $this->setWarning("MSG301", "(ねらいコード)");
            return false;
        }
        if ($this->field["POINT_M_CD"] == "") {
            $this->setWarning("MSG301", "(内容コード)");
            return false;
        }

        //１レコード取得
        $db = Query::dbCheckOut();
        $row = $db->getRow(knjz405Query::getRow($this->field["POINT_L_CD"], $this->field["POINT_M_CD"]), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->cmd == "update" || $this->cmd == "delete") {
            //存在チェック
            if (!($this->point_l_cd == $this->field["POINT_L_CD"] && $this->point_m_cd == $this->field["POINT_M_CD"])) {
                $this->setWarning("MSG308");
                return;
            }
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(内容コード)");
            return false;
        }

        if ($this->cmd == "add" || $this->cmd == "update") {
            //文字数チェック
            if (strlen($this->field["POINT_L_CD"]) > 2 ) {
                $this->setWarning("MSG914", "ねらいコードは2バイトまで");
                return false;
            }
            if (strlen($this->field["POINT_M_CD"]) > 2 ) {
                $this->setWarning("MSG914", "内容コードは 2バイトまで");
                return false;
            }
            if ($this->validate_row_cnt($this->field["REMARK_L"], (4 * 2)) > 1) {
                $this->setWarning("MSG915","(ねらい名称は全角4文字までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["REMARK_M"], (17 * 2)) > 3) {
                $this->setWarning("MSG915","(内容は3行までです)");
                return false;
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            knjz405Query::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            knjz405Query::getInsertQuery($this->field);
            $this->point_l_cd = $this->field["POINT_L_CD"];
            $this->point_m_cd = $this->field["POINT_M_CD"];
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()){
            knjz405Query::getDeleteQuery($this->field);
            $this->point_l_cd = "";
            $this->point_m_cd = "";
            $this->setMessage("MSG201");
        }
    }
}
?>
