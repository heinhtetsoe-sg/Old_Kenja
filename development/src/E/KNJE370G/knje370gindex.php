<?php

require_once('for_php7.php');

require_once('knje370gModel.inc');
require_once('knje370gQuery.inc');

class knje370gController extends Controller {
    var $ModelClassName = "knje370gModel";
    var $ProgramID      = "KNJE370G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370g":
                    $sessionInstance->knje370gModel();
                    $this->callView("knje370gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje370gCtl = new knje370gController;
?>
