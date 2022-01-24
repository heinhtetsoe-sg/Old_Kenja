<?php

require_once('for_php7.php');

require_once('knjd041sModel.inc');
require_once('knjd041sQuery.inc');

class knjd041sController extends Controller {
    var $ModelClassName = "knjd041sModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd041s":
                    $sessionInstance->knjd041sModel();
                    $this->callView("knjd041sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd041sCtl = new knjd041sController;
var_dump($_REQUEST);
?>
