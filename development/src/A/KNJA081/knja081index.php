<?php

require_once('for_php7.php');

require_once('knja081Model.inc');
require_once('knja081Query.inc');

class knja081Controller extends Controller {
    var $ModelClassName = "knja081Model";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja081":
                    $sessionInstance->knja081Model();
                    $this->callView("knja081Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja081Ctl = new knja081Controller;
var_dump($_REQUEST);
?>
