<?php

require_once('for_php7.php');

require_once('knja110_3Model.inc');
require_once('knja110_3Query.inc');

class knja110_3Controller extends Controller {
    var $ModelClassName = "knja110_3Model";
    var $ProgramID      = "KNJA110";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja110_3Form2");
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
                    $this->callView("knja110_3Form1");
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
                    $args["right_src"] = "knja110_3index.php?cmd=list";
                    $args["edit_src"] = "knja110_3index.php?cmd=edit";
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
$knja110_3Ctl = new knja110_3Controller;
//var_dump($_REQUEST);
?>
