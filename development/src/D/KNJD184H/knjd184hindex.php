<?php

require_once('for_php7.php');

require_once('knjd184hModel.inc');
require_once('knjd184hQuery.inc');

class knjd184hController extends Controller {
    var $ModelClassName = "knjd184hModel";
    var $ProgramID      = "KNJD184H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184h":
                    $sessionInstance->knjd184hModel();
                    $this->callView("knjd184hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184hCtl = new knjd184hController;
?>
