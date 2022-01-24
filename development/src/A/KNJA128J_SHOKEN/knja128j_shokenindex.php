<?php

require_once('for_php7.php');

require_once('knja128j_shokenModel.inc');
require_once('knja128j_shokenQuery.inc');

class knja128j_shokenController extends Controller {
    var $ModelClassName = "knja128j_shokenModel";
    var $ProgramID      = "KNJA128J_SHOKEN";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "":
                    $sessionInstance->knja128j_shokenModel();
                    $this->callView("knja128j_shokenForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja128j_shokenCtl = new knja128j_shokenController;
?>
