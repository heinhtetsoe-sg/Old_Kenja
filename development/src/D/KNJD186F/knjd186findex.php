<?php

require_once('for_php7.php');

require_once('knjd186fModel.inc');
require_once('knjd186fQuery.inc');

class knjd186fController extends Controller {
    var $ModelClassName = "knjd186fModel";
    var $ProgramID      = "KNJD186F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186f":
                    $sessionInstance->knjd186fModel();
                    $this->callView("knjd186fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186fCtl = new knjd186fController;
?>
