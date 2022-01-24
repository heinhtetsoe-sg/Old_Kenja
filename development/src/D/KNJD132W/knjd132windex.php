<?php

require_once('for_php7.php');
require_once('knjd132wModel.inc');
require_once('knjd132wQuery.inc');

class knjd132wController extends Controller {
    var $ModelClassName = "knjd132wModel";
    var $ProgramID      = "KNJD132W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "semester":
                case "clear":
                    $this->callView("knjd132wForm1");
                    break 2;
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd132wForm1");
                    break 2;
                case "subform1": //部活動参照
                    $this->callView("knjd132wSubForm1");
                    break 2;
                case "subform2": //委員会参照
                    $this->callView("knjd132wSubForm2");
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
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132W/knjd132windex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjd132windex.php?cmd=edit";
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
$knjd132wCtl = new knjd132wController;
?>
