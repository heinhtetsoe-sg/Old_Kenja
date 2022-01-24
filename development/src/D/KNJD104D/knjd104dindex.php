<?php

require_once('for_php7.php');

require_once('knjd104dModel.inc');
require_once('knjd104dQuery.inc');

class knjd104dController extends Controller {
    var $ModelClassName = "knjd104dModel";
    var $ProgramID      = "KNJD104D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd104d":
                    $sessionInstance->knjd104dModel();
                    $this->callView("knjd104dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd104dCtl = new knjd104dController;
var_dump($_REQUEST);
?>
