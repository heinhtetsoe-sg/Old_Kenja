<?php

require_once('for_php7.php');

require_once('knjd668nModel.inc');
require_once('knjd668nQuery.inc');

class knjd668nController extends Controller {
    var $ModelClassName = "knjd668nModel";
    var $ProgramID      = "KNJD668N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd668n":
                    $sessionInstance->knjd668nModel();
                    $this->callView("knjd668nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd668nCtl = new knjd668nController;
?>
