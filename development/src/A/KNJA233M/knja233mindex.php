<?php

require_once('for_php7.php');

require_once('knja233mModel.inc');
require_once('knja233mQuery.inc');

class knja233mController extends Controller {
    var $ModelClassName = "knja233mModel";
    var $ProgramID      = "KNJA233M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja233m":
                case "gakki":
                    $sessionInstance->knja233mModel();
                    $this->callView("knja233mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja233mCtl = new knja233mController;
//var_dump($_REQUEST);
?>
