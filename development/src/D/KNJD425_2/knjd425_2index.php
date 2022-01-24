<?php

require_once('for_php7.php');

require_once('knjd425_2Model.inc');
require_once('knjd425_2Query.inc');

class knjd425_2Controller extends Controller {
    var $ModelClassName = "knjd425_2Model";
    var $ProgramID      = "KNJD425_2";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform2":
                case "subform2A":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd425_2Form1");
                    break 2;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425_2Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel2();
                    $sessionInstance->setCmd("subform2");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("subform2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425_2Form1");
                    break 2;
                    // //分割フレーム作成
                    // $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD425_2/knjd425_2index.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                    // $args["right_src"] = "knjd425_2index.php?cmd=edit";
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
$knjd425_2Ctl = new knjd425_2Controller;
?>
