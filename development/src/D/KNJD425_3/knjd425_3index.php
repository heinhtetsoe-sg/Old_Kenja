<?php

require_once('for_php7.php');

require_once('knjd425_3Model.inc');
require_once('knjd425_3Query.inc');

class knjd425_3Controller extends Controller {
    var $ModelClassName = "knjd425_3Model";
    var $ProgramID      = "KNJD425_3";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform3":
                case "subform3A":
                case "subform3_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd425_3Form1");
                    break 2;
                case "update3":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425_3Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel3();
                    $sessionInstance->setCmd("subform3");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("subform3");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425_3Form1");
                    break 2;
                    // //分割フレーム作成
                    // $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD425_3/knjd425_3index.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                    // $args["right_src"] = "knjd425_3index.php?cmd=edit";
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
$knjd425_3Ctl = new knjd425_3Controller;
?>
