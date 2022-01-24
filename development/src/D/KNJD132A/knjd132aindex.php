<?php

require_once('for_php7.php');

require_once('knjd132aModel.inc');
require_once('knjd132aQuery.inc');

class knjd132aController extends Controller {
    var $ModelClassName = "knjd132aModel";
    var $ProgramID      = "KNJD132A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "updEdit":
                case "changeSeme":
                    $this->callView("knjd132aForm1");
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
                    $search  = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132A/knjd132aindex.php?cmd=edit") ."&button=1";
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjd132aindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd132aCtl = new knjd132aController;
?>
