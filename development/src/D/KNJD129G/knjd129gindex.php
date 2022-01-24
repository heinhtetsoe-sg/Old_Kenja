<?php

require_once('for_php7.php');


require_once('knjd129gModel.inc');
require_once('knjd129gQuery.inc');

class knjd129gController extends Controller {
    var $ModelClassName = "knjd129gModel";
    var $ProgramID      = "KNJD129G";

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
                    $this->callView("knjd129gForm1");
                    break 2;
                case "chaircd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd129gForm1");
                    break 2;
                case "calc":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd129gForm1");
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
                    $this->callView("knjd129gForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd129gCtl = new knjd129gController;
?>
