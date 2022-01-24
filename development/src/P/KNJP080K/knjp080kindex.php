<?php

require_once('for_php7.php');

require_once('knjp080kModel.inc');
require_once('knjp080kQuery.inc');

class knjp080kController extends Controller {
    var $ModelClassName = "knjp080kModel";
    var $ProgramID      = "KNJP080K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    if (!$sessionInstance->getExecuteModel()){
                        //変更済みの場合は詳細画面に戻る
                        $sessionInstance->setCmd("main");
                        break 1;
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main";
                    $this->callView("knjp080kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp080kCtl = new knjp080kController;
//var_dump($_REQUEST);
?>
