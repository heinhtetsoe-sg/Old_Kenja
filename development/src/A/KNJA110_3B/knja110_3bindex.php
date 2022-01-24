<?php

require_once('for_php7.php');

require_once('knja110_3bModel.inc');
require_once('knja110_3bQuery.inc');

class knja110_3bController extends Controller {
    var $ModelClassName = "knja110_3bModel";
    var $ProgramID      = "KNJA110B";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                case "change":
                    $this->callView("knja110_3bForm2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                   //変更済みの場合は詳細画面に戻る
                   $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                   //変更済みの場合は詳細画面に戻る
                   $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knja110_3bForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = "knja110_3bindex.php?cmd=list";
                    $args["edit_src"] = "knja110_3bindex.php?cmd=edit";
                    $args["rows"] = "35%,65%";
                    View::frame($args,"frame3.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja110_3bCtl = new knja110_3bController;
//var_dump($_REQUEST);
?>
