<?php

require_once('for_php7.php');

require_once('knjj090_2Model.inc');
require_once('knjj090_2Query.inc');

class knjj090_2Controller extends Controller {
    var $ModelClassName = "knjj090_2Model";
    var $ProgramID      = "KNJJ090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj090_2":
                    $sessionInstance->knjj090_2Model();
                    $this->callView("knjj090_2Form1");
                    exit;
                case "insert":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("knjj090_2");
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
$knjj090_2Ctl = new knjj090_2Controller;
?>
