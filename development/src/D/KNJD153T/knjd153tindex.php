<?php

require_once('for_php7.php');

require_once('knjd153tModel.inc');
require_once('knjd153tQuery.inc');

class knjd153tController extends Controller {
    var $ModelClassName = "knjd153tModel";
    var $ProgramID      = "KNJD153T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd153t":
                    $sessionInstance->knjd153tModel();
                    $this->callView("knjd153tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd153tCtl = new knjd153tController;
?>
