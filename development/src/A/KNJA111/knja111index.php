<?php

require_once('for_php7.php');
require_once('knja111Model.inc');
require_once('knja111Query.inc');

class knja111Controller extends Controller {
    var $ModelClassName = "knja111Model";
    var $ProgramID      = "KNJA111";

    function main() {

        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit": 
                case "subEdit": 
                    $this->callView("knja111Form2");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    if ($sessionInstance->getDeleteModel()) {
                        $sessionInstance->setCmd("subEdit");
                    } else {
                        $sessionInstance->setCmd("edit");
                    }
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA111/knja111index.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knja111index.php?cmd=edit";
                    $args["cols"] = "25%,*%";
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
$knja111Ctl = new knja111Controller;
?>
