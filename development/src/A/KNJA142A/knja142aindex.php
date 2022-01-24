<?php

require_once('for_php7.php');

// kanji=漢字
require_once('knja142aModel.inc');
require_once('knja142aQuery.inc');

class knja142aController extends Controller {
    var $ModelClassName = "knja142aModel";
    var $ProgramID      = "KNJA142A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_year":
                case "change_class":
                case "output":
                case "knja142a":
                    $sessionInstance->knja142aModel();
                    $this->callView("knja142aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja142aCtl = new knja142aController;
//var_dump($_REQUEST);
?>
