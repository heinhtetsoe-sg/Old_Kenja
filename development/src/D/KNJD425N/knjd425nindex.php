<?php

require_once('for_php7.php');

require_once('knjd425nModel.inc');
require_once('knjd425nQuery.inc');

class knjd425nController extends Controller {
    var $ModelClassName = "knjd425nModel";
    var $ProgramID      = "KNJD425N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjd425nForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD425N/knjd425nindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1:2:3";
                    $args["right_src"] = "knjd425nindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knjd425nCtl = new knjd425nController;
?>
