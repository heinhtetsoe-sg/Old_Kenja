<?php

require_once('for_php7.php');

require_once('knjz232Model.inc');
require_once('knjz232Query.inc');

class knjz232Controller extends Controller {
    var $ModelClassName = "knjz232Model";
    var $ProgramID      = "KNJZ232";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                    $this->callView("knjz232Form2");
                    break 2;
                case "update":
                case "check":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                    $this->callView("knjz232Form1");
                    break 2;
                case "clear":
                case "list2":
                    $this->callView("knjz232Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz232index.php?cmd=list";
                    $args["right_src"] = "knjz232index.php?cmd=sel";
                    $args["cols"] = "50%,*";
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
$knjz232Ctl = new knjz232Controller;
//var_dump($_REQUEST);
?>
