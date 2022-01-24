<?php

require_once('for_php7.php');

require_once('knjd625aModel.inc');
require_once('knjd625aQuery.inc');

class knjd625aController extends Controller {
    var $ModelClassName = "knjd625aModel";
    var $ProgramID      = "KNJD625A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd625a":
                    $sessionInstance->knjd625aModel();
                    $this->callView("knjd625aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd625aCtl = new knjd625aController;
var_dump($_REQUEST);
?>
