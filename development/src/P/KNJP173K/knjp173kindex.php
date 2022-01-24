<?php

require_once('for_php7.php');
require_once('knjp173kModel.inc');
require_once('knjp173kQuery.inc');

class knjp173kController extends Controller {
    var $ModelClassName = "knjp173kModel";
    var $ProgramID      = "KNJP173K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjp173kForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/P/KNJP173K/knjp173kindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjp173kindex.php?cmd=edit";
                    $args["cols"] = "17%,83%";
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
$knjp173kCtl = new knjp173kController;
?>
