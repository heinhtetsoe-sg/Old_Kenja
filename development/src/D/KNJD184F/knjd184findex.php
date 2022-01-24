<?php

require_once('for_php7.php');

require_once('knjd184fModel.inc');
require_once('knjd184fQuery.inc');

class knjd184fController extends Controller {
    var $ModelClassName = "knjd184fModel";
    var $ProgramID      = "KNJD184F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184f":
                    $sessionInstance->knjd184fModel();
                    $this->callView("knjd184fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184fCtl = new knjd184fController;
?>
