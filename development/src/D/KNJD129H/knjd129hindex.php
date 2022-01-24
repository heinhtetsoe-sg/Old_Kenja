<?php

require_once('for_php7.php');


require_once('knjd129hModel.inc');
require_once('knjd129hQuery.inc');

class knjd129hController extends Controller {
    var $ModelClassName = "knjd129hModel";
    var $ProgramID      = "KNJD129H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "sanSyutu":
                case "disp":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd129hForm1");
                    break 2;
                case "chaircd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd129hForm1");
                    break 2;
                case "calc":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd129hForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd129hForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd129hCtl = new knjd129hController;
?>
