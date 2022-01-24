<?php

require_once('for_php7.php');
require_once('knjd132dModel.inc');
require_once('knjd132dQuery.inc');

class knjd132dController extends Controller {
    var $ModelClassName = "knjd132dModel";
    var $ProgramID      = "KNJD132D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                case "updEdit":
                case "changeSeme":
                    $this->callView("knjd132dForm1");
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
                    $search  = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132D/knjd132dindex.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjd132dindex.php?cmd=edit2";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    return;
                case "teikei1":
                case "teikei2":
                    $this->callView("knjd132dSubForm1");
                    break 2;
                case "ikkatsu":
                    $this->callView("knjd132dIkkatsu"); //一括更新画面
                    break 2;
                case "ikkatsu_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->IkkatsuModel();
                    $sessionInstance->setCmd("ikkatsu");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd132dCtl = new knjd132dController;
?>
