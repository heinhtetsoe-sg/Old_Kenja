<?php

require_once('for_php7.php');

// kanji=漢字
require_once('knja143iModel.inc');
require_once('knja143iQuery.inc');

class knja143iController extends Controller {
    var $ModelClassName = "knja143iModel";
    var $ProgramID      = "KNJA143I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_year":
                case "change_class":
                case "output":
                case "knja143i":
                    $sessionInstance->knja143iModel();
                    $this->callView("knja143iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143iCtl = new knja143iController;
//var_dump($_REQUEST);
?>
