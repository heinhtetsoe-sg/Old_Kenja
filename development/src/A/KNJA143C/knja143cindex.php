<?php

require_once('for_php7.php');

// kanji=漢字
require_once('knja143cModel.inc');
require_once('knja143cQuery.inc');

class knja143cController extends Controller {
    var $ModelClassName = "knja143cModel";
    var $ProgramID      = "KNJA143C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "output":
                case "knja143c":
                    $sessionInstance->knja143cModel();
                    $this->callView("knja143cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143cCtl = new knja143cController;
//var_dump($_REQUEST);
?>
