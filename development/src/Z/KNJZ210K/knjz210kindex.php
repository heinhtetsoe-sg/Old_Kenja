<?php

require_once('for_php7.php');

require_once('knjz210kModel.inc');
require_once('knjz210kQuery.inc');

class knjz210kController extends Controller {
    var $ModelClassName = "knjz210kModel";
    var $ProgramID      = "KNJZ210K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete";
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                    $this->callView("knjz210kForm1");
                    break 2;
                case "chg_grade":
                case "chg_subclass":
                case "chg_asses":
                case "reset":
                case "edit":
                    $this->callView("knjz210kForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz210kindex.php?cmd=list";
                    $args["right_src"] = "knjz210kindex.php?cmd=edit";
                    $args["cols"] = "53%,*";
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
$knjz210kCtl = new knjz210kController;
//var_dump($_REQUEST);
?>
