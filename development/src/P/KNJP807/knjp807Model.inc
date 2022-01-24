<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp807Model extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjp807Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->schoolKind);
        }
        $this->selectdata  = VARS::post("selectdata");
        $this->field = array("NYUUKIN"      =>  VARS::post("NYUUKIN"),      //入金票
                             "COLLECT_M_CD" =>  VARS::post("COLLECT_M_CD"), //入金項目
                             "PAID_FDAY"    =>  VARS::post("PAID_FDAY"),    //生徒入金日期From
                             "PAID_TDAY"    =>  VARS::post("PAID_TDAY"),    //生徒入金日期To
                             "PRINT_DAY"    =>  VARS::post("PRINT_DAY"),    //入金伝票日付
                             "UCHIWAKE"     =>  VARS::post("UCHIWAKE")      //生徒別内訳
                             );

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
