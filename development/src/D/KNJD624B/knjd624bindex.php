<?php

require_once('for_php7.php');

require_once('knjd624bModel.inc');
require_once('knjd624bQuery.inc');

class knjd624bController extends Controller {
    var $ModelClassName = "knjd624bModel";
    var $ProgramID      = "KNJD624B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624b":
                    $sessionInstance->knjd624bModel();
                    $this->callView("knjd624bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624bCtl = new knjd624bController;
?>
