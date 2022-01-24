<?php

require_once('for_php7.php');

require_once('knjp185kModel.inc');
require_once('knjp185kQuery.inc');

class knjp185kController extends Controller {
    var $ModelClassName = "knjp185kModel";
    var $ProgramID      = "KNJP185K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp185kForm1");
                    }
                    $sessionInstance->setCmd("knjp185k");
                    break 2;
                case "":
                case "knjp185k": // メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp185kModel();
                    $this->callView("knjp185kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp185kCtl = new knjp185kController;
//var_dump($_REQUEST);
?>
