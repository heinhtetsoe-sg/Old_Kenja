<?php

require_once('for_php7.php');

require_once('knjd425Model.inc');
require_once('knjd425Query.inc');

class knjd425Controller extends Controller {
    var $ModelClassName = "knjd425Model";
    var $ProgramID      = "KNJD425";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjd425Form1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD425/knjd425index.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1:2";
                    $args["right_src"] = "knjd425index.php?cmd=edit";
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
$knjd425Ctl = new knjd425Controller;
?>
