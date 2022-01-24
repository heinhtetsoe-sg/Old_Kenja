<?php

require_once('for_php7.php');

require_once('knjz235aModel.inc');
require_once('knjz235aQuery.inc');

class knjz235aController extends Controller {
    var $ModelClassName = "knjz235aModel";
    var $ProgramID      = "KNJZ235A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "knjz235a":
                case "grade":
                    $sessionInstance->knjz235aModel();
                    $this->callView("knjz235aForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "read";
                case "clear";
                    $this->callView("knjz235aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjz235aCtl = new knjz235aController;
//var_dump($_REQUEST);
?>
