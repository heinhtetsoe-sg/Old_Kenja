<?php

require_once('for_php7.php');

require_once('knjd132cModel.inc');
require_once('knjd132cQuery.inc');

class knjd132cController extends Controller {
    var $ModelClassName = "knjd132cModel";
    var $ProgramID      = "KNJD132C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "updEdit":
                    $this->callView("knjd132cForm1");
                    break 2;
                case "update":
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
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132C/knjd132cindex.php?cmd=edit") ."&button=1";
                    $args["right_src"] = "knjd132cindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    exit;
                case "teikei1":
                case "teikei2":
                case "teikei3":
                    $this->callView("knjd132cSubForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd132cCtl = new knjd132cController;
?>
