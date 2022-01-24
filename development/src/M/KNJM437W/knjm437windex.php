<?php
require_once('knjm437wModel.inc');
require_once('knjm437wQuery.inc');

class knjm437wController extends Controller {
    var $ModelClassName = "knjm437wModel";
    var $ProgramID      = "KNJM437W";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                case "read":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm437wForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm437wCtl = new knjm437wController;
?>
