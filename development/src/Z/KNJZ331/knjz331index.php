<?php

require_once('for_php7.php');

require_once('knjz331Model.inc');
require_once('knjz331Query.inc');

class knjz331Controller extends Controller {
    var $ModelClassName = "knjz331Model";
    var $ProgramID      = "KNJZ331";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "subMain":
                case "menuCnt";
                    $sessionInstance->knjz331Model();
                    $this->callView("knjz331Form1");
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
$knjz331Ctl = new knjz331Controller;
?>
