<?php

require_once('for_php7.php');

require_once('knja171mModel.inc');
require_once('knja171mQuery.inc');

class knja171mController extends Controller {
    var $ModelClassName = "knja171mModel";
    var $ProgramID      = "KNJA171M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja171m":
                    $sessionInstance->knja171mModel();
                    $this->callView("knja171mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja171mCtl = new knja171mController;
var_dump($_REQUEST);
?>
