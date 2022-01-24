<?php

require_once('for_php7.php');

require_once('knjd135rModel.inc');
require_once('knjd135rQuery.inc');

class knjd135rController extends Controller {
    var $ModelClassName = "knjd135rModel";
    var $ProgramID      = "KNJD135R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "value_set":
                    $this->callView("knjd135rForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "teikei":
                    $this->callView("knjd135rSubForm1");
                    break 2;
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
$knjd135rCtl = new knjd135rController;
?>
