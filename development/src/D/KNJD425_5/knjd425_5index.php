<?php

require_once('for_php7.php');

require_once('knjd425_5Model.inc');
require_once('knjd425_5Query.inc');

class knjd425_5Controller extends Controller {
    var $ModelClassName = "knjd425_5Model";
    var $ProgramID      = "KNJD425_5";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform5":
                case "subform5A":
                case "subform5_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd425_5Form1");
                    break 2;
                case "update5":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425_5Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel5();
                    $sessionInstance->setCmd("subform5");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("subform5");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425_5Form1");
                    break 2;
                    // //分割フレーム作成
                    // $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD425_5/knjd425_5index.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                    // $args["right_src"] = "knjd425_5index.php?cmd=edit";
                    // $args["cols"] = "20%,80%";
                    // View::frame($args);
                    // exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425_5Ctl = new knjd425_5Controller;
?>
