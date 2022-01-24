<?php

require_once('for_php7.php');

require_once('knjd653cModel.inc');
require_once('knjd653cQuery.inc');

class knjd653cController extends Controller {
    var $ModelClassName = "knjd653cModel";
    var $ProgramID      = "KNJD653C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd653c":
                case "gakki":
                    $sessionInstance->knjd653cModel();
                    $this->callView("knjd653cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd653cCtl = new knjd653cController;
var_dump($_REQUEST);
?>
