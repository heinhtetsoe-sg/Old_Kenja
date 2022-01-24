<?php

require_once('for_php7.php');

require_once('knjd625fModel.inc');
require_once('knjd625fQuery.inc');

class knjd625fController extends Controller {
    var $ModelClassName = "knjd625fModel";
    var $ProgramID      = "KNJD625F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "changeYear":
                case "knjd625f_2":
                case "knjd625f":
                    $sessionInstance->knjd625fModel();
                    $this->callView("knjd625fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd625fCtl = new knjd625fController;
var_dump($_REQUEST);
?>
