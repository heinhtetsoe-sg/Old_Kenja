<?php

require_once('for_php7.php');
require_once('knjd137hModel.inc');
require_once('knjd137hQuery.inc');

class knjd137hController extends Controller {
    var $ModelClassName = "knjd137hModel";
    var $ProgramID      = "KNJD137H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                case "relode":
                    if($sessionInstance->pattern == 1){
                        $this->callView("knjd137hForm1");
                    } else if($sessionInstance->pattern == 2){
                        $this->callView("knjd137hForm2");
                    } else if($sessionInstance->pattern == 3){
                        $this->callView("knjd137hForm3");
                    } else {
                        $this->callView("knjd137hFormBlank");
                    }
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel2();
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD137H/knjd137hindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1" ."&SCHOOL_KIND=H";
                    $args["right_src"] = "knjd137hindex.php?cmd=edit";
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
$knjd137hCtl = new knjd137hController;
?>
