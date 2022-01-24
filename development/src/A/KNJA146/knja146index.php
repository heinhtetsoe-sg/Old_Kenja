<?php

require_once('for_php7.php');

// kanji=漢字
require_once('knja146Model.inc');
require_once('knja146Query.inc');

class knja146Controller extends Controller {
    var $ModelClassName = "knja146Model";
    var $ProgramID      = "KNJA146";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "output":
                case "knja146":
                    $sessionInstance->knja146Model();
                    $this->callView("knja146Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja146Ctl = new knja146Controller;
//var_dump($_REQUEST);
?>
