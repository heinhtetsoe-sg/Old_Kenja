<?php

require_once('for_php7.php');

require_once('knjd064kModel.inc');
require_once('knjd064kQuery.inc');

class knjd064kController extends Controller {
    var $ModelClassName = "knjd064kModel";
    var $ProgramID      = "KNJD064K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd064k":
                case "semechg":
                    $sessionInstance->knjd064kModel();
                    $this->callView("knjd064kForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd064kModel();
                    $this->callView("knjd064kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd064kCtl = new knjd064kController;
var_dump($_REQUEST);
?>
