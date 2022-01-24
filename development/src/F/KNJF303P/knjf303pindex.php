<?php

require_once('for_php7.php');

require_once('knjf303pModel.inc');
require_once('knjf303pQuery.inc');

class knjf303pController extends Controller {
    var $ModelClassName = "knjf303pModel";
    var $ProgramID      = "KNJF303P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf303p":
                    $sessionInstance->knjf303pModel();
                    $this->callView("knjf303pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf303pCtl = new knjf303pController;
?>
