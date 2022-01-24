<?php

require_once('for_php7.php');

require_once('knjj091_2Model.inc');
require_once('knjj091_2Query.inc');

class knjj091_2Controller extends Controller {
    var $ModelClassName = "knjj091_2Model";
    var $ProgramID      = "KNJJ091";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj091_2":
                    $sessionInstance->setAccessLogDetail("S", "KNJJ091_2");
                    $sessionInstance->knjj091_2Model();
                    $this->callView("knjj091_2Form1");
                    exit;
                case "insert":
                    $sessionInstance->setAccessLogDetail("U", "KNJJ091_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("knjj091_2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj091_2Ctl = new knjj091_2Controller;
?>
