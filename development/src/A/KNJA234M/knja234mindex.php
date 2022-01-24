<?php

require_once('for_php7.php');

require_once('knja234mModel.inc');
require_once('knja234mQuery.inc');

class knja234mController extends Controller {
    var $ModelClassName = "knja234mModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja234m":
                    $sessionInstance->knja234mModel();
                    $this->callView("knja234mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja234mCtl = new knja234mController;
var_dump($_REQUEST);
?>
