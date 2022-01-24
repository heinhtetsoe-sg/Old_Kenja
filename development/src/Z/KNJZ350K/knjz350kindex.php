<?php

require_once('for_php7.php');

require_once('knjz350kModel.inc');
require_once('knjz350kQuery.inc');

class knjz350kController extends Controller {
    var $ModelClassName = "knjz350kModel";
    var $ProgramID      = "KNJZ350K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjz350kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz350kCtl = new knjz350kController;
//var_dump($_REQUEST);
?>
