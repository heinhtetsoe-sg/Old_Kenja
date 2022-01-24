<?php

require_once('for_php7.php');

require_once('knjd669nModel.inc');
require_once('knjd669nQuery.inc');

class knjd669nController extends Controller {
    var $ModelClassName = "knjd669nModel";
    var $ProgramID      = "KNJD669N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd669n":
                    $sessionInstance->knjd669nModel();
                    $this->callView("knjd669nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd669nCtl = new knjd669nController;
?>
