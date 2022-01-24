<?php

require_once('for_php7.php');

require_once('knja082aModel.inc');
require_once('knja082aQuery.inc');

class knja082aController extends Controller {
    var $ModelClassName = "knja082aModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja082a":
                    $sessionInstance->knja082aModel();
                    $this->callView("knja082aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja082aCtl = new knja082aController;
var_dump($_REQUEST);
?>
