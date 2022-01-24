<?php

require_once('for_php7.php');

require_once('knjd183pModel.inc');
require_once('knjd183pQuery.inc');

class knjd183pController extends Controller {
    var $ModelClassName = "knjd183pModel";
    var $ProgramID      = "KNJD183P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd183p":
                    $sessionInstance->knjd183pModel();
                    $this->callView("knjd183pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd183pCtl = new knjd183pController;
?>
