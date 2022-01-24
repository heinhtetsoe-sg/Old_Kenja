<?php

require_once('for_php7.php');

require_once('knjd061tModel.inc');
require_once('knjd061tQuery.inc');

class knjd061tController extends Controller {
    var $ModelClassName = "knjd061tModel";
    var $ProgramID      = "KNJD061T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd061t":
                    $sessionInstance->knjd061tModel();
                    $this->callView("knjd061tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd061tCtl = new knjd061tController;
var_dump($_REQUEST);
?>
