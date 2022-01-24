<?php

require_once('for_php7.php');

require_once('knjd184pModel.inc');
require_once('knjd184pQuery.inc');

class knjd184pController extends Controller {
    var $ModelClassName = "knjd184pModel";
    var $ProgramID      = "KNJD184P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184p":
                    $sessionInstance->knjd184pModel();
                    $this->callView("knjd184pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd184pCtl = new knjd184pController;
?>
