<?php

require_once('for_php7.php');

require_once('knjd614bModel.inc');
require_once('knjd614bQuery.inc');

class knjd614bController extends Controller {
    var $ModelClassName = "knjd614bModel";
    var $ProgramID      = "KNJD614B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd614b_2":
                case "knjd614b":
                    $sessionInstance->knjd614bModel();
                    $this->callView("knjd614bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd614bCtl = new knjd614bController;
var_dump($_REQUEST);
?>
