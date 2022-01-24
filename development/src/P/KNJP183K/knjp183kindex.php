<?php

require_once('for_php7.php');

require_once('knjp183kModel.inc');
require_once('knjp183kQuery.inc');

class knjp183kController extends Controller {
    var $ModelClassName = "knjp183kModel";
    var $ProgramID      = "KNJP183K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("knjp183k");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp183kForm1");
                    }
                    $sessionInstance->setCmd("knjp183k");
                    break 2;
                case "":
                case "knjp183k": // メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp183kModel();
                    $this->callView("knjp183kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp183kCtl = new knjp183kController;
//var_dump($_REQUEST);
?>
