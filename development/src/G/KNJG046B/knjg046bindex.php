<?php

require_once('for_php7.php');

require_once('knjg046bModel.inc');
require_once('knjg046bQuery.inc');

class knjg046bController extends Controller {
    var $ModelClassName = "knjg046bModel";
    var $ProgramID      = "KNJG046B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjg046bModel();
                    $this->callView("knjg046bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjg046bCtl = new knjg046bController;
?>
