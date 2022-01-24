<?php

require_once('for_php7.php');

require_once('knjb150tModel.inc');
require_once('knjb150tQuery.inc');

class knjb150tController extends Controller {
    var $ModelClassName = "knjb150tModel";
    var $ProgramID      = "KNJB150T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb150t":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb150tModel();      //コントロールマスタの呼び出し
                    $this->callView("knjb150tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjb150tCtl = new knjb150tController;
//var_dump($_REQUEST);
?>
