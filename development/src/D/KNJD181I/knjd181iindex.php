<?php

require_once('for_php7.php');

require_once('knjd181iModel.inc');
require_once('knjd181iQuery.inc');

class knjd181iController extends Controller {
    var $ModelClassName = "knjd181iModel";
    var $ProgramID      = "KNJD181I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd181i":
                    $sessionInstance->knjd181iModel();
                    $this->callView("knjd181iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd181iCtl = new knjd181iController;
?>
