<?php

require_once('for_php7.php');

require_once('knjg084Model.inc');
require_once('knjg084Query.inc');

class knjg084Controller extends Controller {
    var $ModelClassName = "knjg084Model";
    var $ProgramID      = "KNJG084";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "change":
                    $this->callView("knjg084Form1");
                    break 2;
                case "sel":
                case "left":
                case "change2":
                    $this->callView("knjg084Form2");
                    break 2;
                case "copylastyear":
                    $sessionInstance->InsertCopyLastYearModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjg084index.php?cmd=list";
                    $args["right_src"] = "knjg084index.php?cmd=sel";
                    $args["cols"] = "35%,*";
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
$knjg084Ctl = new knjg084Controller;
?>
