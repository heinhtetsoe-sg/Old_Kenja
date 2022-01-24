<?php

require_once('for_php7.php');

require_once('knjl020kModel.inc');
require_once('knjl020kQuery.inc');

class knjl020kController extends Controller {
    var $ModelClassName = "knjl020kModel";
    var $ProgramID      = "KNJL020K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "search":
                case "cancel":
                case "back1":
                case "next1":
                case "reference":
                case "testDivChange":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl020kForm1");
                    break 2;
                case "dialog":
                    $this->callView("knjl020kForm2");
                    break 2;
                case "insert":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                case "pre_update":
                case "next_update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl020kForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("dialog");
                    break 1;
                case "delete2":
                    $sessionInstance->getDelete2Model();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("dialog");
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
$KNJL020KCtl = new knjl020kController;

//var_dump($_REQUEST);
?>
