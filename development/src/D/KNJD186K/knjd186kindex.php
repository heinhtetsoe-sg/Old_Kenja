<?php

require_once('for_php7.php');

require_once('knjd186kModel.inc');
require_once('knjd186kQuery.inc');

class knjd186kController extends Controller {
    var $ModelClassName = "knjd186kModel";
    var $ProgramID      = "KNJD186K";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd186k";
                    $sessionInstance->knjd186kModel();
                    $this->callView("knjd186kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186kCtl = new knjd186kController;
?>
