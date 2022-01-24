<?php

require_once('for_php7.php');

require_once('knjb0012Model.inc');
require_once('knjb0012Query.inc');

class knjb0012Controller extends Controller {
    var $ModelClassName = "knjb0012Model";
    var $ProgramID        = "KNJB0012";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "subMain":
                case "chgSeme":
                    $sessionInstance->knjb0012Model();
                    $this->callView("knjb0012Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subMain");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0012Ctl = new knjb0012Controller;
//var_dump($_REQUEST);
?>
