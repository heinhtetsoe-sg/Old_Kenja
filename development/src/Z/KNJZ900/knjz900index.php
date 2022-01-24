<?php

require_once('for_php7.php');

require_once('knjz900Model.inc');
require_once('knjz900Query.inc');

class knjz900Controller extends Controller {
    var $ModelClassName = "knjz900Model";
    var $ProgramID      = "KNJZ900";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz900":
                case "main":
                case "reset":
                    $sessionInstance->knjz900Model();
                    $this->callView("knjz900Form1");
                    exit;
                case "insert":
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjz900");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("knjz900");
                    break 1;
                case "subform1":
                case "subform1A":
                case "subform1_reset":
                    $this->callView("knjz900SubForm1");
                    break 2;
                case "subform1_update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateSubModel1();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1A");
                    break 1;
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
$knjz900Ctl = new knjz900Controller;
?>
