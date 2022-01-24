<?php

require_once('for_php7.php');

require_once('knja223mModel.inc');
require_once('knja223mQuery.inc');

class knja223mController extends Controller {
    var $ModelClassName = "knja223mModel";
    var $ProgramID      = "KNJA223M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja223m":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja223mModel();        //コントロールマスタの呼び出し
                    $this->callView("knja223mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja223mCtl = new knja223mController;
//var_dump($_REQUEST);
?>
