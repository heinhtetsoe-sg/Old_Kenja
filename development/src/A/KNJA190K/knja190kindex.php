<?php

require_once('for_php7.php');

require_once('knja190kModel.inc');
require_once('knja190kQuery.inc');

class knja190kController extends Controller {
    var $ModelClassName = "knja190kModel";
    var $ProgramID      = "KNJA190K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knja190k":
                    $sessionInstance->knja190kModel();
                    $this->callView("knja190kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja190kCtl = new knja190kController;
var_dump($_REQUEST);
?>
