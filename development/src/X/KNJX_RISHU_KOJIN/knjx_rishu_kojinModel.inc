<?php

require_once('for_php7.php');

class knjx_rishu_kojinModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $schregno = "";
    var $name = "";
    var $entyear = "";
    var $field = array();
    var $control;

    function knjx_rishu_kojinModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->schregno);
            unset($this->getSearch_div);
            unset($this->exp_year);
            unset($this->name);
            unset($this->Auth);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        if (VARS::get("SEND_PRGRID")) {
            $this->getPrgId       = VARS::get("PRGRID");
            $this->schregno       = VARS::get("SCHREGNO");
            $this->getSearch_div  = VARS::get("SEARCH_DIV");
            $this->exp_year       = VARS::get("EXE_YEAR");
            $this->name           = VARS::get("NAME");
            $this->Auth           = VARS::get("AUTH");
        }

        $this->field = array("NAME"             =>  VARS::post("NAME"),
                             "ZIPCD"            =>  VARS::post("ZIPCD"),
                             "ADDR1"            =>  VARS::post("ADDR1"),
                             "ADDR2"            =>  VARS::post("ADDR2"),
                             "TELNO"            =>  VARS::post("TELNO"),
                             "TELNO2"           =>  VARS::post("TELNO2"),
                             "ADDR_FLG"         =>  VARS::post("ADDR_FLG"),
                             "AREACD"           =>  VARS::post("AREACD"),
                             "JOBCD"            =>  VARS::post("JOBCD"),
                             
                             "BASE_REMARK1"     =>  VARS::post("BASE_REMARK1"),
                             "BASE_REMARK2"     =>  VARS::post("BASE_REMARK2"),
                             "BASE_REMARK5"     =>  VARS::post("BASE_REMARK5"),
                             
                             "SCHOOLING_DATE"   =>  VARS::post("SCHOOLING_DATE"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","(名前は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["ZIPCD"]) > 8) {
            $this->setWarning("MSG915","(郵便番号は8バイトまでです)");
            return false;
        }
        if (strlen($this->field["ADDR1"]) > 150) {
            $this->setWarning("MSG915","(住所1は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["ADDR2"]) > 75) {
            $this->setWarning("MSG915","(住所2は75バイトまでです)");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","(電話番号1は14バイトまでです)");
            return false;
        }
        if (strlen($this->field["TELNO2"]) > 14) {
            $this->setWarning("MSG915","(電話番号2は14バイトまでです)");
            return false;
        }
        if (strlen($this->field["AREACD"]) > 2) {
            $this->setWarning("MSG915","(地区コードは2バイトまでです)");
            return false;
        }
        if (strlen($this->field["JOBCD"]) > 2) {
            $this->setWarning("MSG915","(職業コードは2バイトまでです)");
            return false;
        }
        
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        //SCHREG_SEND_ADDRESS_DATの更新
        knjx_rishu_kojinQuery::getUpdateQuery($this->field, $this, $this->schregSend_data);
        //SCHREG_BASE_DETAIL_MSTの更新
        knjx_rishu_kojinQuery::getUpdateBaseQuery($this->field, $this);
        //SCHREG_BASE_YEAR_DETAIL_MSTの更新
        knjx_rishu_kojinQuery::getUpdateBaseYearQuery($this->field, $this);

        return ;
    }
}
?>
