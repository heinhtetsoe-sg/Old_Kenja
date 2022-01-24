<?php

require_once('for_php7.php');

require_once('knje432aModel.inc');
require_once('knje432aQuery.inc');

class knje432aController extends Controller {
    var $ModelClassName = "knje432aModel";
    var $ProgramID      = "KNJE432A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje432a":
                    $sessionInstance->knje432aModel();
                    $this->callView("knje432aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje432aCtl = new knje432aController;
?>
