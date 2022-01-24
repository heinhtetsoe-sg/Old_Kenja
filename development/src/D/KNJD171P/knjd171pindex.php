<?php

require_once('for_php7.php');

require_once('knjd171pModel.inc');
require_once('knjd171pQuery.inc');

class knjd171pController extends Controller {
    var $ModelClassName = "knjd171pModel";
    var $ProgramID      = "KNJD171P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd171p":
                    $sessionInstance->knjd171pModel();
                    $this->callView("knjd171pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd171pCtl = new knjd171pController;
?>
