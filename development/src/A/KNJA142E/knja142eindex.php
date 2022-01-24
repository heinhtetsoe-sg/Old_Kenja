<?php

require_once('for_php7.php');

require_once('knja142eModel.inc');
require_once('knja142eQuery.inc');

class knja142eController extends Controller {
    var $ModelClassName = "knja142eModel";
    var $ProgramID      = "KNJA142E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knja142e":
                    $sessionInstance->knja142eModel();
                    $this->callView("knja142eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja142eCtl = new knja142eController;
?>
