<?php

require_once('for_php7.php');

// kanji=漢字
require_once('knja143rModel.inc');
require_once('knja143rQuery.inc');

class knja143rController extends Controller {
    var $ModelClassName = "knja143rModel";
    var $ProgramID      = "KNJA143R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "output":
                case "knja143r":
                    $sessionInstance->knja143rModel();
                    $this->callView("knja143rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143rCtl = new knja143rController;
//var_dump($_REQUEST);
?>
