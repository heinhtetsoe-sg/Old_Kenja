<?php

require_once('for_php7.php');

require_once('knjaopsModel.inc');
require_once('knjaopsQuery.inc');

class knjaopsController extends Controller {
    var $ModelClassName = "knjaopsModel";
    var $ProgramID      = "KNJAOPS";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "https":
                case "knjaops":
                case "knjaops2":
                    $sessionInstance->knjaopsModel();
                    $this->callView("knjaopsForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjaops");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjaopsCtl = new knjaopsController;
var_dump($_REQUEST);
?>
