<?php

require_once('for_php7.php');


class knjl351w_2Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;

    /**
     * メンバ変数の初期化
     */
    function knjl351w_2Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("OUTPUT_DIV"       =>  VARS::post("OUTPUT_DIV"),
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"          =>  VARS::post("TESTDIV"),
                             "TESTDIV2"         =>  VARS::post("TESTDIV2"),
                             "RUIKEI_DIV"       =>  VARS::post("RUIKEI_DIV")
                             );
        $this->entexamYear = CTRL_YEAR + 1;

        $this->cmd  = VARS::request("cmd");
        $this->selectData = explode(",", VARS::post("selectdata"));
        $this->field["TESTDIV2"] = $this->field["TESTDIV2"] ? $this->field["TESTDIV2"] : "0";

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function &getMainModel() {
    }

    //県への報告
    function &getCancelModel() {
        knjl351w_2Query::getUpdateReportFixedCancel($this);
        $this->setMessage("MSG201");
    }
}
?>
