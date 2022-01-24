<?php

require_once('for_php7.php');

require_once('knjm437mModel.inc');
require_once('knjm437mQuery.inc');

class knjm437mController extends Controller {
    var $ModelClassName = "knjm437mModel";
    var $ProgramID      = "KNJM437M";     //プログラムID

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
                    $this->callView("knjm437mForm1");
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
$knjm437mCtl = new knjm437mController;
?>
