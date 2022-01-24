<?php

require_once('for_php7.php');

require_once('knjm505dModel.inc');
require_once('knjm505dQuery.inc');

class knjm505dController extends Controller {
    var $ModelClassName = "knjm505dModel";
    var $ProgramID      = "KNJM505D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm505d":
                    $sessionInstance->knjm505dModel();
                    $this->callView("knjm505dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjm505dCtl = new knjm505dController;
var_dump($_REQUEST);
?>
