<?php

require_once('for_php7.php');

require_once('knjta020_07Model.inc');
require_once('knjta020_07Query.inc');

class knjta020_07Controller extends Controller {
    var $ModelClassName = "knjta020_07Model";
    var $ProgramID      = "KNJTA020_07";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "sanshou":
                case "search":
                case "main":
                case "cancel":
                case "stk_cancel":
                case "subForm":
                case "kateiGet":
                case "kateiGet2":
                case "KyuhuSubmit":
                    $this->callView("knjta020_07Form1");
                    break 2;
                case "subForm1":
                case "submain":
                    $this->callView("knjta020_07SubForm1");
                    break 2;
                case "chkForm":
                    $sessionInstance->getChkForm();
                    $sessionInstance->setCmd("subForm");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "kyuhu_update":
                    $sessionInstance->getUpdateKyuhuModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "kyuhu_delete":
                    $sessionInstance->getDeleteKyuhuModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
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
$knjta020_07Ctl = new knjta020_07Controller;
//var_dump($_REQUEST);
?>
