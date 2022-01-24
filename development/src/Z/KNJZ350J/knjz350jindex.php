<?php

require_once('for_php7.php');

require_once('knjz350jModel.inc');
require_once('knjz350jQuery.inc');

class knjz350jController extends Controller {
    var $ModelClassName = "knjz350jModel";
    var $ProgramID      = "KNJZ350J";

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
                    $this->callView("knjz350jForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz350jCtl = new knjz350jController;
//var_dump($_REQUEST);
?>
