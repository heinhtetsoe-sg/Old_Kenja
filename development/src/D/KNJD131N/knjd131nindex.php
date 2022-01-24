<?php

require_once('for_php7.php');
require_once('knjd131nModel.inc');
require_once('knjd131nQuery.inc');

class knjd131nController extends Controller {
    var $ModelClassName = "knjd131nModel";
    var $ProgramID      = "KNJD131N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                    $this->callView("knjd131nForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd131nForm1");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "reset":
                    $this->callView("knjd131nForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD131N/knjd131nindex.php?cmd=edit") ."&button=3";
                    $args["right_src"] = "knjd131nindex.php?cmd=edit&init=1";
                    $args["cols"] = "23%,*";
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
$knjd131nCtl = new knjd131nController;
//var_dump($_REQUEST);
?>
