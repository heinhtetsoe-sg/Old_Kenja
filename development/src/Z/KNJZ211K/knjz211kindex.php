<?php
require_once('knjz211kModel.inc');
require_once('knjz211kQuery.inc');

class knjz211kController extends Controller {

    var $ModelClassName = "knjz211kModel";
    var $ProgramID      = "KNJZ211K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->getCopyModel();
                    $this->callView("knjz211kForm1");
                    break 2;
                case "reset":
                case "edit":
                    $this->callView("knjz211kForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz211kForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz211kindex.php?cmd=list";
                    $args["right_src"] = "knjz211kindex.php?cmd=edit";
                    $args["cols"] = "40%,60%";
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
$knjz211kCtl = new knjz211kController;
//var_dump($_REQUEST);
?>
