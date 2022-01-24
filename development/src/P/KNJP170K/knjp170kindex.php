<?php

require_once('for_php7.php');

require_once('knjp170kModel.inc');
require_once('knjp170kQuery.inc');

class knjp170kController extends Controller {
    var $ModelClassName = "knjp170kModel";
    var $ProgramID      = "KNJP170K";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "send":
                    $sessionInstance->getSendModel();
                    break 2;
                /* Tip Message 取得用Model */
                //異動情報取得
                case "sendT":
                    $sessionInstance->getSendModelTransfer();
                    break 2;
                //奨学金情報取得
                case "sendG":
                    $sessionInstance->getSendModelGrant();
                    break 2;

                case "maingrade":
                case "mainclass":
                case "main":
                case "copy":
                case "keigen":
                case "cancel":
                    $sessionInstance->getMainModel();
                    $this->callView("knjp170kForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp170kCtl = new knjp170kController;
?>
