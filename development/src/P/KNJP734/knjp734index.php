<?php

require_once('for_php7.php');

require_once('knjp734Model.inc');
require_once('knjp734Query.inc');

class knjp734Controller extends Controller {
    var $ModelClassName = "knjp734Model";
    var $ProgramID      = "KNJP734";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("knjp734");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp734Form1");
                    }
                    $sessionInstance->setCmd("knjp734");
                    break 2;
                case "":
                case "knjp734": // メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp734Model();
                    $this->callView("knjp734Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp734Ctl = new knjp734Controller;
//var_dump($_REQUEST);
?>
