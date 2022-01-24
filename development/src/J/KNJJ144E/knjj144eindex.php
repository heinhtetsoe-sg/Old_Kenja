<?php

require_once('for_php7.php');

require_once('knjj144eModel.inc');
require_once('knjj144eQuery.inc');

class knjj144eController extends Controller {
    var $ModelClassName = "knjj144eModel";
    var $ProgramID      = "KNJJ144E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjj144eModel();
                    $this->callView("knjj144eForm1");
                    exit;
                case "exec":
                    $sessionInstance->getExecModel();
                    $this->callView("knjj144eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj144eCtl = new knjj144eController;
?>
