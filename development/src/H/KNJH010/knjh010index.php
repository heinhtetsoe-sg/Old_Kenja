<?php

require_once('for_php7.php');
require_once('knjh010Model.inc');
require_once('knjh010Query.inc');

class knjh010Controller extends Controller {
    var $ModelClassName = "knjh010Model";
    var $ProgramID      = "KNJH010";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjh010Form1");
                    break 2;
                case "subform1": //通知表所見参照
                case "reset2":
                    $this->callView("knjh010SubForm1");
                    break 2;
                case "update":
                    //NO001
                    if (!$sessionInstance->auth){
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update2":
                    //NO001
                    if (!$sessionInstance->auth){
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("reset2");
                    break 1;
                case "delete":
                    //NO001
                    if (!$sessionInstance->auth){
                        $this->checkAuth(DEF_UPDATE_RESTRICT);
                    }
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $this->callView("knjh010Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "back2":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP3/index.php?PROGRAMID=KNJH160&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH010/knjh010index.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjh010index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    View::frame($args);
                    break 2;
                case "":
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/H/KNJH010/knjh010index.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjh010index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
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
$knjh010Ctl = new knjh010Controller;
?>