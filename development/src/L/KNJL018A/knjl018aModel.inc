<?php

require_once('for_php7.php');


class knjl018aModel extends model
{
    var $cmd;
    var $year;
    var $preischoolcd;
    var $preischoolClassCd;
    var $selected;

    var $applicantdiv;
    var $testdiv;
    var $exam_type;

    function knjl018aModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR + 1;
        
        $this->preischoolcd = VARS::post("PRISCHOOLCD");
        $this->preischoolClassCd = VARS::post("PRISCHOOL_CLASS_CD");
        $this->selected = explode(",", VARS::post("CATEGORY_SELECTED_DATA"));
        
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv = VARS::post("TESTDIV");
        $this->exam_type = "1";

        if (VARS::get("PROGRAMID")) {
            unset($this->preischoolcd);
            unset($this->preischoolClassCd);
            unset($this->selected);
        }

    }

    //受験番号をセット
    function &getMainModel()
    {

        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }

    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        if ($this->preischoolcd == "" && $this->preischoolClassCd == "") {
            $this->setMessage("MSG301");
            return;
        }
        $chkret = knjl018aQuery::chkPriSchCode($this);
        if (!$chkret) {
            $this->setMessage("MSG301");
            return;
        }

        knjl018aQuery::deleteQuery($this);
        $arrychkdat = knjl018aQuery::checkUpdateBseDatil($this, array_values($this->selected));
        foreach ($arrychkdat as $value){
            if ($value["SPACE_CD"] == -1) {
                knjl018aQuery::insertBaseDetailDat($this, $value["EXAMNO"]);
            } else if ($value["SPACE_CD"] > 0) {
                knjl018aQuery::updateBaseDetailDat($this, $value["EXAMNO"], $value["SPACE_CD"]);
            }
        }
        $this->setMessage("MSG201");
    }

    function hex2bin( $str ) {
        $sbin = "";
        $len = strlen( $str );
        for ( $i = 0; $i < $len; $i += 2 ) {
            $sbin .= pack( "H*", substr( $str, $i, 2 ) );
        }

        return $sbin;
    }
}
?>
