<?php

require_once('for_php7.php');

require_once('knjg046eModel.inc');
require_once('knjg046eQuery.inc');

class knjg046eController extends Controller {
    var $ModelClassName = "knjg046eModel";
    var $ProgramID      = "KNJG046E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjg046eModel();
                    $this->callView("knjg046eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjg046eCtl = new knjg046eController;
?>
