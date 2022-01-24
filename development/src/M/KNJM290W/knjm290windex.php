<?php

require_once('for_php7.php');

require_once('knjm290wModel.inc');
require_once('knjm290wQuery.inc');

class knjm290wController extends Controller {
    var $ModelClassName = "knjm290wModel";
    var $ProgramID      = "KNJM290W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm290w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm290wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm290wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm290wCtl = new knjm290wController;
//var_dump($_REQUEST);
?>

