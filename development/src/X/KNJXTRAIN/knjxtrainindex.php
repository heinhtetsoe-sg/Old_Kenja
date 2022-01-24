<?php

require_once('for_php7.php');

require_once('knjxtrainModel.inc');
require_once('knjxtrainQuery.inc');

class knjxtrainController extends Controller {
    var $ModelClassName = "knjxtrainModel";
    var $ProgramID      = "knjxtrain";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxtrainForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxtrainCtl = new knjxtrainController;
?>
