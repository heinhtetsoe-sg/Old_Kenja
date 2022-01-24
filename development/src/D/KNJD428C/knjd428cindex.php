<?php

require_once('for_php7.php');
require_once('knjd428cModel.inc');
require_once('knjd428cQuery.inc');

class knjd428cController extends Controller {
    var $ModelClassName = "knjd428cModel";
    var $ProgramID      = "KNJD428C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                case "updEdit":
                case "defaultStf":
                        $this->callView("knjd428cForm1");
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
                    $search  = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD428C/knjd428cindex.php?cmd=edit") ."&button=1&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    $args["right_src"] = "knjd428cindex.php?cmd=edit2";
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
$knjd428cCtl = new knjd428cController;
?>
