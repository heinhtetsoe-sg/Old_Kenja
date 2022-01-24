<?php

require_once('for_php7.php');
class knjp736Model extends Model {
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

    function knjp736Model()
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
                                 "YEAR"             => VARS::request("YEAR"),
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

        $this->updField = array();
        if ($this->cmd == "update") {
            foreach ($this->updData as $key => $val) {
                $this->updField[] = array("SEQ"             => $val["SEQ"],
                                          "PAID_MONEY"      => VARS::post("PAID_MONEY_{$val["SEQ"]}"),
                                          "PAID_MONEY_DATE" => VARS::post("PAID_MONEY_DATE_{$val["SEQ"]}")
                                          );
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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

            knjp736Query::deletePaid($db, $this);
            knjp736Query::insertPaid($db, $this);

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

}
?>
