<?php

require_once('for_php7.php');

require_once('knjd187fModel.inc');
require_once('knjd187fQuery.inc');

class knjd187fController extends Controller {
    var $ModelClassName = "knjd187fModel";
    var $ProgramID      = "KNJD187F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd187f":
                    $sessionInstance->knjd187fModel();
                    $this->callView("knjd187fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187fCtl = new knjd187fController;
?>
