<?php

require_once('for_php7.php');

require_once('knjp081kModel.inc');
require_once('knjp081kQuery.inc');

class knjp081kController extends Controller {
    var $ModelClassName = "knjp081kModel";
    var $ProgramID      = "KNJP081K";

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
                case "read":
                case "main";
                    $this->callView("knjp081kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp081kCtl = new knjp081kController;
//var_dump($_REQUEST);
?>
