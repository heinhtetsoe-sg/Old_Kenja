<?php

require_once('for_php7.php');
require_once('knjf012Model.inc');
require_once('knjf012Query.inc');

class knjf012Controller extends Controller {
    var $ModelClassName = "knjf012Model";
    var $ProgramID      = "KNJF012";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjf012Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $this->callView("knjf012Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/F/KNJF012/knjf012index.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["useSpecial_Support_Hrclass"] || $sessionInstance->Properties["useFi_Hrclass"]) {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    }
                    $args["right_src"] = "knjf012index.php?cmd=edit";
                    $args["cols"] = "19%,81%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf012Ctl = new knjf012Controller;
?>
