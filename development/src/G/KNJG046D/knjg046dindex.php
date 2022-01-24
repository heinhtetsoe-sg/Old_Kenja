<?php

require_once('for_php7.php');

require_once('knjg046dModel.inc');
require_once('knjg046dQuery.inc');

class knjg046dController extends Controller {
    var $ModelClassName = "knjg046dModel";
    var $ProgramID      = "KNJG046D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjg046dModel();
                    $this->callView("knjg046dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjg046dCtl = new knjg046dController;
?>
