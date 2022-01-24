<?php

require_once('for_php7.php');

require_once('knjx400sModel.inc');
require_once('knjx400sQuery.inc');

class knjx400sController extends Controller {
    var $ModelClassName = "knjx400sModel";
    var $ProgramID      = "KNJX400S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjx400s":
                    $sessionInstance->knjx400sModel();
                    $this->callView("knjx400sForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjx400s");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjx400sCtl = new knjx400sController;
var_dump($_REQUEST);
?>
