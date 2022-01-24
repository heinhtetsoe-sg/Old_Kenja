<?php

require_once('for_php7.php');

require_once('knjb060aModel.inc');
require_once('knjb060aQuery.inc');

class knjb060aController extends Controller {
    var $ModelClassName = "knjb060aModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb060a":
                    $this->callView("knjb060aForm1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb060aCtl = new knjb060aController;
//var_dump($_REQUEST);
?>
