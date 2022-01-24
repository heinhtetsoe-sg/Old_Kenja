<?php

require_once('for_php7.php');

require_once('knjp722Model.inc');
require_once('knjp722Query.inc');

class knjp722Controller extends Controller {
    var $ModelClassName = "knjp722Model";
    var $ProgramID      = "KNJP722";     //プログラムID

    function main() {
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
                //減免情報取得
                case "sendG":
                    $sessionInstance->getSendModelGenmen();
                    break 2;
                case "cmdStart":
                case "maingrade":
                case "mainclass":
                case "main":
                case "cancel":
                case "reSize":
                case "calc":
                    $sessionInstance->getMainModel();
                    $this->callView("knjp722Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp722Ctl = new knjp722Controller;
?>
