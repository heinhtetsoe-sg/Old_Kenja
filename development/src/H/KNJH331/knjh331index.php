<?php

require_once('for_php7.php');

require_once('knjh331Model.inc');
require_once('knjh331Query.inc');

class knjh331Controller extends Controller {
    var $ModelClassName = "knjh331Model";
    var $ProgramID      = "KNJH331";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjh331Form2");
                    break 2;
                case "list":
                case "divChange":
                    $this->callView("knjh331Form1");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjh331index.php?cmd=list";
                    $args["right_src"] = "knjh331index.php?cmd=edit";
                    $args["cols"] = "50%,50%";
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
$knjh331Ctl = new knjh331Controller;
//var_dump($_REQUEST);
?>
