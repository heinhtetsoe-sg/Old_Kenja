<?php

require_once('for_php7.php');

require_once('knjz290_2Model.inc');
require_once('knjz290_2Query.inc');

class knjz290_2Controller extends Controller {
    var $ModelClassName = "knjz290_2Model";
    var $ProgramID      = "KNJZ290";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "change":
                    $this->callView("knjz290_2Form2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz290_2Form1");
                    break 2;
                case "list2":
                    $this->callView("knjz290_2SubForm1");
                    break 2;
                case "subform1":
                case "subform1_clear":
                    $this->callView("knjz290_2SubForm1");
                    break 2;
                case "subform1_update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform1_add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform1_delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform2":
                case "subform2_clear":
                    $this->callView("knjz290_2SubForm2");
                    break 2;
                case "subform2_update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateSubModel2();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform2");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz290_2index.php?cmd=list";
                    $args["right_src"] = "knjz290_2index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz290_2Ctl = new knjz290_2Controller;
//var_dump($_REQUEST);
?>
