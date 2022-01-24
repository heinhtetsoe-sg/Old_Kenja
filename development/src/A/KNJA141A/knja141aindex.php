<?php

require_once('for_php7.php');

require_once('knja141aModel.inc');
require_once('knja141aQuery.inc');

class knja141aController extends Controller {
    var $ModelClassName = "knja141aModel";
    var $ProgramID      = "KNJA141A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "change_class":
                    $sessionInstance->knja141aModel();       //コントロールマスタの呼び出し
                    $this->callView("knja141aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja141aCtl = new knja141aController;
//var_dump($_REQUEST);
?>
