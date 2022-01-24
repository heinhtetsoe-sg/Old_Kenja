<?php

require_once('for_php7.php');

require_once('knjd124jModel.inc');
require_once('knjd124jQuery.inc');

class knjd124jController extends Controller {
    var $ModelClassName = "knjd124jModel";
    var $ProgramID      = "KNJD124J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "semester":
                case "classcd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd124jForm1");
                   break 2;
                case "update":
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
$knjd124jCtl = new knjd124jController;
?>
