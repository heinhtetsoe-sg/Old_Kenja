<?php

require_once('for_php7.php');
require_once('knjd132lModel.inc');
require_once('knjd132lQuery.inc');

class knjd132lController extends Controller {
    var $ModelClassName = "knjd132lModel";
    var $ProgramID      = "KNJD132L";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "semester":
                case "updEdit":
                case "clear":
                    $this->callView("knjd132lForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132L/knjd132lindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1" ."&SCHOOL_KIND=J";
                    $args["right_src"] = "knjd132lindex.php?cmd=edit";
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
$knjd132lCtl = new knjd132lController;
?>
