<?php
require_once('knje372oModel.inc');
require_once('knje372oQuery.inc');

class knje372oController extends Controller {
    var $ModelClassName = "knje372oModel";
    var $ProgramID      = "KNJE372O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "select":
                case "updEdit":
                case "clear":
                case "changeClass":
                    $this->callView("knje372oForm2");
                    break 2;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                case "changeOyear":
                    $this->callView("knje372oForm1");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $sessionInstance->boot_flg = true;
                    $args["left_src"] = "knje372oindex.php?cmd=list";
                    $args["right_src"] = "knje372oindex.php?cmd=edit";
                    $args["cols"] = "45%,55%";
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
$knje372oCtl = new knje372oController;
?>
