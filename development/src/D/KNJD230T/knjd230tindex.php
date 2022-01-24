<?php

require_once('for_php7.php');

require_once('knjd230tModel.inc');
require_once('knjd230tQuery.inc');

class knjd230tController extends Controller {
    var $ModelClassName = "knjd230tModel";
    var $ProgramID      = "KNJD230T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd230t":
                    $sessionInstance->knjd230tModel();
                    $this->callView("knjd230tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd230tCtl = new knjd230tController;
?>
