<?php

require_once('for_php7.php');
require_once('knjd131kModel.inc');
require_once('knjd131kQuery.inc');

class knjd131kController extends Controller {
    var $ModelClassName = "knjd131kModel";
    var $ProgramID      = "KNJD131K";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "grade":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd131kForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD131K/knjd131kindex.php?cmd=edit") ."&button=1"."&SES_FLG=2";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjd131kindex.php?cmd=edit";
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
$knjd131kCtl = new knjd131kController;
?>
