<?php

require_once('for_php7.php');

require_once('knja190aModel.inc');
require_once('knja190aQuery.inc');

class knja190aController extends Controller {
    var $ModelClassName = "knja190aModel";
    var $ProgramID      = "KNJA190A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "hukusiki":
                case "change_class":
                case "knja190a":
                    $sessionInstance->knja190aModel();
                    $this->callView("knja190aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja190aCtl = new knja190aController;
var_dump($_REQUEST);
?>
