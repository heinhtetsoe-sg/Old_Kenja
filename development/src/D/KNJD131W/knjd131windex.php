<?php

require_once('for_php7.php');
require_once('knjd131wModel.inc');
require_once('knjd131wQuery.inc');

class knjd131wController extends Controller {
    var $ModelClassName = "knjd131wModel";
    var $ProgramID      = "KNJD131W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "clear":
                    $this->callView("knjd131wForm1");
                    break 2;
                case "edit":
                case "edit2":
                case "hanei":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd131wForm1");
                    break 2;
                case "reference_first":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd131wSubForm1");
                    break 2;
                case "subform1": //部活動参照
                    $this->callView("knjd131wSubForm1");
                    break 2;
                case "subform2": //委員会参照
                    $this->callView("knjd131wSubForm2");
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD131W/knjd131windex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjd131windex.php?cmd=edit";
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
$knjd131wCtl = new knjd131wController;
?>
