<?php

require_once('for_php7.php');

// kanji=漢字
require_once('knja143sModel.inc');
require_once('knja143sQuery.inc');

class knja143sController extends Controller {
    var $ModelClassName = "knja143sModel";
    var $ProgramID      = "KNJA143S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "output":
                case "knja143s":
                    $sessionInstance->knja143sModel();
                    $this->callView("knja143sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143sCtl = new knja143sController;
//var_dump($_REQUEST);
?>
