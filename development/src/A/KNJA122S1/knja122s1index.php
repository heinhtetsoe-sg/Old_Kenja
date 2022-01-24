<?php

require_once('for_php7.php');

require_once('knja122s1Model.inc');
require_once('knja122s1Query.inc');

class knja122s1Controller extends Controller {
    var $ModelClassName = "knja122s1Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "https":
                case "knja122s1":
                case "knja122s12":
                    $sessionInstance->knja122s1Model();
                    $this->callView("knja122s1Form1");
                    exit;
                case "sslApplet":
                    $sessionInstance->knja122s1Model();
                    $this->callView("knja122s1Form1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja122s1");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja122s1Ctl = new knja122s1Controller;
var_dump($_REQUEST);
?>
