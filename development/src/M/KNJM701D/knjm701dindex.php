<?php

require_once('for_php7.php');

require_once('knjm701dModel.inc');
require_once('knjm701dQuery.inc');

class knjm701dController extends Controller {
    var $ModelClassName = "knjm701dModel";
    var $ProgramID      = "KNJM701D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjm701d";
                    $sessionInstance->knjm701dModel();
                    $this->callView("knjm701dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm701dCtl = new knjm701dController;
?>
