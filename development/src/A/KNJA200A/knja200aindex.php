<?php

require_once('for_php7.php');

require_once('knja200aModel.inc');
require_once('knja200aQuery.inc');

class knja200aController extends Controller {
    var $ModelClassName = "knja200aModel";
    var $ProgramID      = "KNJA200A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "defaultUpd":
                    $sessionInstance->updPrgDefaultVal("KNJA200A", SCHOOLCD, SCHOOLKIND);
                    $sessionInstance->setCmd("knja200a");
                    break 1;
                case "":
                case "knja200a":
                    $sessionInstance->knja200aModel();
                    $this->callView("knja200aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja200aCtl = new knja200aController;
var_dump($_REQUEST);
?>
