<?php

require_once('for_php7.php');

require_once('knjd186eModel.inc');
require_once('knjd186eQuery.inc');

class knjd186eController extends Controller {
    var $ModelClassName = "knjd186eModel";
    var $ProgramID      = "KNJD186E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186e":
                    $sessionInstance->knjd186eModel();
                    $this->callView("knjd186eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186eCtl = new knjd186eController;
?>
