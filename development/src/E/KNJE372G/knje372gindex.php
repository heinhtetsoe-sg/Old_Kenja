<?php
require_once('knje372gModel.inc');
require_once('knje372gQuery.inc');

class knje372gController extends Controller {
    var $ModelClassName = "knje372gModel";
    var $ProgramID      = "KNJE372G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje372g":
                    $sessionInstance->knje372gModel();
                    $this->callView("knje372gForm1");
                    exit;
                case "exec":
                    $sessionInstance->getExecModel();
                    $this->callView("knje372gForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje372gCtl = new knje372gController;
//var_dump($_REQUEST);
?>
