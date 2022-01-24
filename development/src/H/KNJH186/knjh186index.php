<?php

require_once('for_php7.php');
require_once('knjh186Model.inc');
require_once('knjh186Query.inc');

class knjh186Controller extends Controller {
    var $ModelClassName = "knjh186Model";
    var $ProgramID      = "KNJH186";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reset":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh186Form2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjh186Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH186/knjh186index.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjh186index.php?cmd=right_list";
                    $args["edit_src"]  = "knjh186index.php?cmd=edit";
                    $args["cols"] = "30%,70%";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame2.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh186Ctl = new knjh186Controller;
?>
