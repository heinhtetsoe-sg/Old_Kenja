<?php

require_once('for_php7.php');

require_once('knjd428Model.inc');
require_once('knjd428Query.inc');

class knjd428Controller extends Controller {
    var $ModelClassName = "knjd428Model";
    var $ProgramID      = "KNJD428";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                case "updEdit":
                    $this->callView("knjd428Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search  = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD428/knjd428index.php?cmd=edit") ."&button=1&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    $args["right_src"] = "knjd428index.php?cmd=edit2";
                    $args["cols"] = "20%,80%";
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
$knjd428Ctl = new knjd428Controller;
?>
