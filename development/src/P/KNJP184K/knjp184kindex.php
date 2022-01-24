<?php

require_once('for_php7.php');

require_once('knjp184kModel.inc');
require_once('knjp184kQuery.inc');

class knjp184kController extends Controller {
    var $ModelClassName = "knjp184kModel";
    var $ProgramID      = "KNJP184K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp184kForm1");
                    }
                    $sessionInstance->setCmd("knjp184k");
                    break 2;
                case "":
                case "knjp184k": // メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp184kModel();
                    $this->callView("knjp184kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp184kCtl = new knjp184kController;
//var_dump($_REQUEST);
?>
