<?php

require_once('for_php7.php');

require_once('knjd139bModel.inc');
require_once('knjd139bQuery.inc');

class knjd139bController extends Controller {
    var $ModelClassName = "knjd139bModel";
    var $ProgramID      = "KNJD139B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "clear":
                    $this->callView("knjd139bForm1");
                    break 2;
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd139bForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
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
                case "back":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/D/KNJD139B/knjd139bindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knjd139bindex.php?cmd=edit";
                    $args["cols"] = "25%,*";
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
$knjd139bCtl = new knjd139bController;
?>
