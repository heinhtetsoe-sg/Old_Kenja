<?php

require_once('for_php7.php');


require_once('knjd128v_mikomiModel.inc');
require_once('knjd128v_mikomiQuery.inc');

class knjd128v_mikomiController extends Controller {
    var $ModelClassName = "knjd128v_mikomiModel";
    var $ProgramID      = "KNJD128V_MIKOMI";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd128v_mikomiForm1");
                   break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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
$knjd128v_mikomiCtl = new knjd128v_mikomiController;
?>
