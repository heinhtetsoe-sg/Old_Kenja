<?php

require_once('for_php7.php');

require_once('knjf305pModel.inc');
require_once('knjf305pQuery.inc');

class knjf305pController extends Controller {
    var $ModelClassName = "knjf305pModel";
    var $ProgramID      = "KNJF305P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf305p":
                    $sessionInstance->knjf305pModel();
                    $this->callView("knjf305pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf305pCtl = new knjf305pController;
?>
