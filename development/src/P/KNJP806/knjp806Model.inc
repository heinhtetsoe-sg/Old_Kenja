<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp806Model extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjp806Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->selectdata  = VARS::post("selectdata");
        $this->field = array("SYOSEKI"      =>  VARS::post("SYOSEKI"),      //書籍入金票
                             "PAID_FDAY"    =>  VARS::post("PAID_FDAY"),    //生徒入金日期From
                             "PAID_TDAY"    =>  VARS::post("PAID_TDAY"),    //生徒入金日期To
                             "PRINT_DAY"    =>  VARS::post("PRINT_DAY"),    //入金伝票日付
                             "UCHIWAKE"     =>  VARS::post("UCHIWAKE")      //生徒別内訳
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
