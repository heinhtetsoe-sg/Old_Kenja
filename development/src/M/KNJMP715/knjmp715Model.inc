<?php

require_once('for_php7.php');

class knjmp715Model extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $schregno;
    var $exp_mcd;
    var $exp_lcd;
    var $exp_mname;
    var $money_due;
    var $s_exist_flg;
    var $collect_grp_cd;
    var $paid_flg;
    var $pay_div;

    function knjmp715Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->claimNo);
        }

        if(strlen(VARS::get("SCHREGNO"))){
            $this->schregno = VARS::get("SCHREGNO");
            $this->search_div = VARS::get("SEARCH_DIV");
            unset($this->claimNo);
        }

        if (VARS::get("init") != "") {
            unset($this->schregno);
            unset($this->claimNo);
        }

        $this->mst_field = array("SCHREGNO"         => VARS::request("SCHREGNO"),
                                 "SELECT_YEAR"      => VARS::request("SELECT_YEAR"),
                                 "SLIP_NO"          => VARS::request("SLIP_NO"),
                                 "PAY_DIV"          => VARS::post("PAY_DIV"),
                                 "SLIP_DATE"        => VARS::post("SLIP_DATE"),
                                 "GROPCD"           => VARS::post("GROPCD"),
                                 "JUGYOU"           => VARS::post("JUGYOU"),
                                 "CANCEL_DATE"      => VARS::post("CANCEL_DATE"),
                                 "LIMIT_DATE"       => VARS::post("LIMIT_DATE"),
                                 "maxSeq"           => VARS::post("maxSeq"),
                                 "GROPCD"           => VARS::post("GROPCD")
                                 );

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "changeYear") {
            $this->mst_field["SLIP_NO"] = "";
            $this->mst_field["PAY_DIV"] = "";
            $this->mst_field["SLIP_DATE"] = "";
            $this->mst_field["GROPCD"] = "";
            $this->mst_field["JUGYOU"] = "";
            $this->mst_field["CANCEL_DATE"] = "";
            $this->mst_field["LIMIT_DATE"] = "";
            $this->mst_field["maxSeq"] = "";
            $this->mst_field["GROPCD"] = "";
        }

        $this->updField = array();
        if ($this->cmd == "update" || $this->cmd == "delete") {
            for ($updCnt = 1; $updCnt <= $this->mst_field["maxSeq"]; $updCnt++) {
                $this->updField[$updCnt] = array("DELCHK"           => VARS::post("DELCHK_{$updCnt}"),          //削除
                                                 "COLLECT_LM_CD"    => VARS::post("COLLECT_LM_CD_{$updCnt}"),   //LMコード
                                                 "MONEY_DUE"        => VARS::post("MONEY_DUE_{$updCnt}"),       //単価
                                                 "COLLECT_CNT"      => VARS::post("COLLECT_CNT_{$updCnt}")      //数量
                                                 );
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function validateInput()
    {
        if (!strlen($this->schregno)) {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjmp715Query::deleteDue($db, $this);
            knjmp715Query::insertDue($db, $this);

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjmp715Query::deleteDue($db, $this);
            knjmp715Query::insertSeq($db, $this);

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    function &getPrintModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjmp715Query::insertPrintHist($db, $this);

            $db->commit();
            Query::dbCheckIn($db);
        }
    }
}
?>
