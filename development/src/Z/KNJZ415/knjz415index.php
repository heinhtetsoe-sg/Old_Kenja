<?php

require_once('for_php7.php');

require_once('knjz415Model.inc');
require_once('knjz415Query.inc');

class knjz415Controller extends Controller {
    var $ModelClassName = "knjz415Model";
    var $ProgramID      = "KNJZ415";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz415Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                    $this->callView("knjz415Form1");
                    break 2;
                case "clear":
                case "list2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz415Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz415index.php?cmd=list";
                    $args["right_src"] = "knjz415index.php?cmd=sel";
                    $args["cols"] = "45%,*";
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
$knjz415Ctl = new knjz415Controller;
//var_dump($_REQUEST);
?>
