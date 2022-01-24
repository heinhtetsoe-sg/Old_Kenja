<?php

require_once('for_php7.php');

class knjz040mModel extends Model {
    var $field      = array();
    var $cmd;
    var $natpubpri_cd;
    var $area_div_cd;

    function knjz040mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->natpubpri_cd);
            unset($this->area_div_cd);
            unset($this->area_cd);
        }

        $this->field = array("NATPUBPRI_CD"   =>  VARS::post("NATPUBPRI_CD"),
                             "NATPUBPRI_NAME" =>  VARS::post("NATPUBPRI_NAME"),
                             "NATPUBPRI_ABBV" =>  VARS::post("NATPUBPRI_ABBV"),
                             "AREA_DIV_CD"    =>  VARS::post("AREA_DIV_CD"),
                             "AREA_DIV_NAME"  =>  VARS::post("AREA_DIV_NAME"),
                             "AREA_DIV_ABBV"  =>  VARS::post("AREA_DIV_ABBV"),
                             "AREA_CD"        =>  VARS::post("AREA_CD"),
                             "AREA_NAME"      =>  VARS::post("AREA_NAME"),
                             "AREA_ABBV"      =>  VARS::post("AREA_ABBV")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("SEND_FLG") == "1"){
            $this->natpubpri_cd = VARS::get("NATPUBPRI_CD");
            $this->area_div_cd  = VARS::get("AREA_DIV_CD");
            $this->area_cd      = VARS::get("AREA_CD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須
        if (trim($this->field["NATPUBPRI_CD"]) == "" || trim($this->field["NATPUBPRI_NAME"]) == "" || 
            trim($this->field["AREA_DIV_CD"]) == "" || trim($this->field["AREA_DIV_NAME"]) == "" || 
            trim($this->field["AREA_CD"]) == "" || trim($this->field["AREA_NAME"]) == "") {
            $this->setWarning("MSG301");
            return false;
        }
        //キーの更新チェック
        if (($this->cmd == "delete") && ($this->natpubpri_cd != $this->field["NATPUBPRI_CD"] || 
                                         $this->area_div_cd  != $this->field["AREA_DIV_CD"] || 
                                         $this->area_cd      != $this->field["AREA_CD"])      ) {
            $this->setWarning("MSG308");
            return false;
        }
        //頭ゼロ埋め
        if (strlen($this->field["AREA_DIV_CD"]) > 0 && strlen($this->field["AREA_DIV_CD"]) <= 2) {
            $this->field["AREA_DIV_CD"] = sprintf("%02d", $this->field["AREA_DIV_CD"]);
        }
        if (strlen($this->field["AREA_CD"]) > 0 && strlen($this->field["AREA_CD"]) <= 2) {
            $this->field["AREA_CD"] = sprintf("%02d", $this->field["AREA_CD"]);
        }
        //更新対象の有無のチェック
        $db = Query::dbCheckOut();
        $query = knjz040mQuery::getEntexamNatpubpriMst($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (($this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        if (strlen($this->field["AREA_DIV_CD"])) {
            $query = knjz040mQuery::getEntexamAreaDivMst($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (($this->cmd == "delete") && !is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        if (strlen($this->field["AREA_CD"])) {
            $query = knjz040mQuery::getEntexamAreaMst($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (($this->cmd == "delete") && !is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        Query::dbCheckIn($db);

        //文字数チェック
        if (strlen($this->field["NATPUBPRI_CD"]) > 1) {
            $this->setWarning("MSG915", "(国公私立コード) 1バイトまでです。");
            return false;
        } elseif (strlen($this->field["AREA_DIV_CD"]) > 2) {
            $this->setWarning("MSG915", "(所在地区分コード) 2バイトまでです。");
            return false;
        } elseif (strlen($this->field["AREA_CD"]) > 2) {
            $this->setWarning("MSG915", "(所在地コード) 2バイトまでです。");
            return false;

        } elseif (strlen($this->field["NATPUBPRI_NAME"]) > 9 ) {
            $this->setWarning("MSG915", "(国公私立名称) 9バイトまでです。");
            return false;
        } elseif (strlen($this->field["AREA_DIV_NAME"]) > 30 ) {
            $this->setWarning("MSG915", "(所在地区分名称) 30バイトまでです。");
            return false;
        } elseif (strlen($this->field["AREA_NAME"]) > 30 ) {
            $this->setWarning("MSG915", "(所在地名称) 30バイトまでです。");
            return false;

        } elseif (strlen($this->field["NATPUBPRI_ABBV"]) > 9 ) {
            $this->setWarning("MSG915", "(国公私立略称) 9バイトまでです。");
            return false;
        } elseif (strlen($this->field["AREA_DIV_ABBV"]) > 30 ) {
            $this->setWarning("MSG915", "(所在地区分略称) 30バイトまでです。");
            return false;
        } elseif (strlen($this->field["AREA_ABBV"]) > 30 ) {
            $this->setWarning("MSG915", "(所在地略称) 30バイトまでです。");
            return false;
        }
        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            knjz040mQuery::getUpdateMstQuery($this);
            $this->setMessage("MSG201");
            $this->natpubpri_cd = $this->field["NATPUBPRI_CD"];
            $this->area_div_cd  = $this->field["AREA_DIV_CD"];
            $this->area_cd      = $this->field["AREA_CD"];
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz040mQuery::getDeleteQuery($this);
            unset($this->natpubpri_cd);
            unset($this->area_div_cd);
            unset($this->area_cd);
            $this->setMessage("MSG201");
        }
    }
}
?>
