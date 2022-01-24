<?php

require_once('for_php7.php');

require_once('knjm504wModel.inc');
require_once('knjm504wQuery.inc');

class knjm504wController extends Controller {
    var $ModelClassName = "knjm504wModel";
    var $ProgramID      = "KNJM504W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm504w":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm504wModel();   //コントロールマスタの呼び出し
                    $this->callView("knjm504wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm504wCtl = new knjm504wController;
var_dump($_REQUEST);
?>
