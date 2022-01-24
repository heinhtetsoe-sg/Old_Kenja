<?php

require_once('for_php7.php');

require_once('knjm506dModel.inc');
require_once('knjm506dQuery.inc');

class knjm506dController extends Controller {
    var $ModelClassName = "knjm506dModel";
    var $ProgramID      = "KNJM506D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm506d":
                    $sessionInstance->knjm506dModel();
                    $this->callView("knjm506dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjm506dCtl = new knjm506dController;
var_dump($_REQUEST);
?>
