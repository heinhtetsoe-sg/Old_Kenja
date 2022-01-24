<?php

require_once('for_php7.php');

require_once('knjz236Model.inc');
require_once('knjz236Query.inc');

class knjz236Controller extends Controller {
    var $ModelClassName = "knjz236Model";
    var $ProgramID      = "KNJZ236";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz236Form2");
                    break 2;
                case "update":
                case "check":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                    $this->callView("knjz236Form1");
                    break 2;
                case "clear":
                case "list2":
                    $this->callView("knjz236Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz236index.php?cmd=list";
                    $args["right_src"] = "knjz236index.php?cmd=sel";
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
$knjz236Ctl = new knjz236Controller;
//var_dump($_REQUEST);
?>
