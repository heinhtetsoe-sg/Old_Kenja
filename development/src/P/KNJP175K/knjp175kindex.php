<?php

require_once('for_php7.php');

require_once('knjp175kModel.inc');
require_once('knjp175kQuery.inc');

class knjp175kController extends Controller {
    var $ModelClassName = "knjp175kModel";
    var $ProgramID      = "KNJP175K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjp175kForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjp175kCtl = new knjp175kController;
?>
