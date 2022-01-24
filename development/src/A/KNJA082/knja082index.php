<?php

require_once('for_php7.php');

require_once('knja082Model.inc');
require_once('knja082Query.inc');

class knja082Controller extends Controller {
    var $ModelClassName = "knja082Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja082":
                    $sessionInstance->knja082Model();
                    $this->callView("knja082Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja082Ctl = new knja082Controller;
var_dump($_REQUEST);
?>
