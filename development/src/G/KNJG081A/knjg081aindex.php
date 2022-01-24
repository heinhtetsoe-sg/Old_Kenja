<?php

require_once('for_php7.php');

require_once('knjg081aModel.inc');
require_once('knjg081aQuery.inc');

class knjg081aController extends Controller {
    var $ModelClassName = "knjg081aModel";
    var $ProgramID      = "KNJG081A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "main":
                case "reset":
                    $this->callView("knjg081aForm1");
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
                    $sessionInstance->setCmd("init");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjg081aCtl = new knjg081aController;
?>
