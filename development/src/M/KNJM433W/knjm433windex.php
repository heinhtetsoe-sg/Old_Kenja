<?php

require_once('for_php7.php');

require_once('knjm433wModel.inc');
require_once('knjm433wQuery.inc');

class knjm433wController extends Controller {
    var $ModelClassName = "knjm433wModel";
    var $ProgramID      = "KNJM433W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjm433w");
                    break 1;
                case "":
                case "knjm433w":
                    $sessionInstance->knjm433wModel();
                    $this->callView("knjm433wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm433wCtl = new knjm433wController;
var_dump($_REQUEST);
?>
